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
            rewardTokens=((0 until receipts.length()).map { receipts.optString(it) }.toSet()+last)-"")
    }.getOrDefault(Progress())

    fun encode(progress: Progress): String {
        val records=JSONObject()
        progress.records.forEach { (id,r) -> records.put(id,JSONObject().put("count",r.count).put("best",r.bestCm)) }
        return JSONObject().put("version",2).put("coins",progress.coins).put("owned",progress.owned.joinToString(","))
            .put("equipped",progress.equipped).put("deepest",progress.deepest).put("records",records)
            .put("reward_token",progress.lastRewardToken).put("reward_tokens",JSONArray((progress.rewardTokens+progress.lastRewardToken-"").sorted()))
            .put("xp",progress.xp).put("level",progress.level).toString()
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
        if(next==current) true else save(next)
    }
    fun changeRod(id: Int): Boolean = synchronized(lock) {
        val current=load()
        save(if(id in current.owned)FishingLogic.equip(current,id) else FishingLogic.buy(current,id))
    }
    fun save(progress: Progress, token: String? = null): Boolean = synchronized(lock) {
        val current=load()
        if(token!=null && (token==current.lastRewardToken || token in current.rewardTokens)) true
        else prefs.edit().putString("progress",FishingSaveCodec.encode(progress)).commit()
    }
    private companion object { val lock=Any() }
}
