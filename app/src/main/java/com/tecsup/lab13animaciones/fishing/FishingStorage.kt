package com.tecsup.lab13animaciones.fishing

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Compatible codec: the v1 preference name, key, IDs and fields remain intact. */
object FishingSaveCodec {
    fun decode(raw: String): Progress = runCatching {
        val data=JSONObject(raw)
        val owned=data.optString("owned","0").split(',').mapNotNull(String::toIntOrNull)
            .filter { id -> rods.any { it.id==id } }.toSet()+0
        val equipped=data.optInt("equipped",0).takeIf { it in owned } ?: 0
        val records=data.optJSONObject("records") ?: JSONObject()
        val receipts=data.optJSONArray("reward_tokens") ?: JSONArray()
        val last=data.optString("reward_token","")
        Progress(coins=data.optInt("coins",0).coerceAtLeast(0),owned=owned,equipped=equipped,
            records=records.keys().asSequence().mapNotNull { id -> records.optJSONObject(id)?.let {
                id to FishRecord(it.optInt("count").coerceAtLeast(0),it.optInt("best").coerceAtLeast(0))
            } }.toMap(),deepest=data.optInt("deepest",0).coerceIn(0,500),lastRewardToken=last,
            xp=data.optInt("xp",0).coerceAtLeast(0),
            rewardTokens=((0 until receipts.length()).map { receipts.optString(it) }.toSet()+last)-"",
            missionCatches=data.optInt("mission_catches",0).coerceAtLeast(0),
            missionSpecies=readSet(data.optJSONArray("mission_species")),
            missionDeepest=data.optInt("mission_deepest",0).coerceIn(0,500),
            completedMissions=readSet(data.optJSONArray("completed_missions")))
    }.getOrDefault(Progress())

    private fun readSet(array: JSONArray?): Set<String> =
        if(array==null) emptySet() else (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }.toSet()

    fun encode(progress: Progress): String {
        val records=JSONObject()
        progress.records.forEach { (id,r) -> records.put(id,JSONObject().put("count",r.count).put("best",r.bestCm)) }
        return JSONObject().put("version",3).put("coins",progress.coins).put("owned",progress.owned.joinToString(","))
            .put("equipped",progress.equipped).put("deepest",progress.deepest).put("records",records)
            .put("reward_token",progress.lastRewardToken).put("reward_tokens",JSONArray((progress.rewardTokens+progress.lastRewardToken-"").sorted()))
             .put("xp",progress.xp).put("level",progress.level)
            .put("mission_catches",progress.missionCatches).put("mission_species",JSONArray(progress.missionSpecies.sorted()))
            .put("mission_deepest",progress.missionDeepest).put("completed_missions",JSONArray(progress.completedMissions.sorted())).toString()
    }
}

/** A single committed entry atomically stores rewards, XP, receipts, equipment and records.
 * The process-wide lock also serializes separate Activity/storage instances during recreation.
 */
class FishingStorage(context: Context, name: String = "fishing_legends_v1") {
    private val prefs=context.applicationContext.getSharedPreferences(name,Context.MODE_PRIVATE)
    fun load(): Progress = synchronized(lock) { FishingSaveCodec.decode(prefs.getString("progress","{}") ?: "{}") }
    fun capture(catch: Catch): Boolean = synchronized(lock) {
        val current=load()
        val next=FishingLogic.reward(current,catch)
        save(next)
    }
    fun explore(depth: Int): Boolean = synchronized(lock) {
        val current=load()
        val next=FishingLogic.explore(current,depth)
        save(next)
    }
    fun changeRod(id: Int): Boolean = synchronized(lock) {
        val current=load()
        save(if(id in current.owned)FishingLogic.equip(current,id) else FishingLogic.buy(current,id))
    }
    fun save(progress: Progress, token: String? = null): Boolean = synchronized(lock) {
        val current=load()
        // A failed commit can still update SharedPreferences' in-memory cache. Recommit the
        // current receipt on retry; its presence alone is not proof that disk writing succeeded.
        if(token!=null && (token==current.lastRewardToken || token in current.rewardTokens))
            prefs.edit().putString("progress",FishingSaveCodec.encode(current)).commit()
        else prefs.edit().putString("progress",FishingSaveCodec.encode(progress)).commit()
    }
    private companion object { val lock=Any() }
}
