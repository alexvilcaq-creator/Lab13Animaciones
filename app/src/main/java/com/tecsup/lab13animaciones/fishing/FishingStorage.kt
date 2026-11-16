package com.tecsup.lab13animaciones.fishing

import android.content.Context
import org.json.JSONObject

/** One atomic preference entry keeps coins, equipment, records and reward token consistent. */
class FishingStorage(context: Context, name: String = "fishing_legends_v1") {
    private val prefs = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    fun load(): Progress = runCatching {
        val data = JSONObject(prefs.getString("progress", "{}") ?: "{}")
        val owned = data.optString("owned", "0").split(',').mapNotNull(String::toIntOrNull)
            .filter { id -> rods.any { it.id == id } }.toSet() + 0
        val equipped = data.optInt("equipped",0).takeIf { it in owned } ?: 0
        val records = data.optJSONObject("records") ?: JSONObject()
        Progress(data.optInt("coins",0).coerceAtLeast(0), owned, equipped,
            species.mapNotNull { fish -> records.optJSONObject(fish.id)?.let {
                fish.id to FishRecord(it.optInt("count").coerceAtLeast(0),it.optInt("best").coerceAtLeast(0))
            } }.toMap(), data.optInt("deepest",0).coerceIn(0,500), data.optString("reward_token", ""))
    }.getOrDefault(Progress())

    @Synchronized fun capture(catch: Catch): Boolean = save(FishingLogic.reward(load(), catch), catch.token)

    @Synchronized fun changeRod(id: Int): Boolean {
        val current = load()
        return save(if (id in current.owned) FishingLogic.equip(current,id) else FishingLogic.buy(current,id))
    }

    @Synchronized fun save(progress: Progress, token: String? = null): Boolean {
        if (token != null && token == load().lastRewardToken) return true
        val records = JSONObject()
        progress.records.forEach { (id, record) -> records.put(id, JSONObject().put("count",record.count).put("best",record.bestCm)) }
        val data = JSONObject().put("coins",progress.coins).put("owned",progress.owned.joinToString(","))
            .put("equipped",progress.equipped).put("deepest",progress.deepest).put("records",records).put("reward_token",progress.lastRewardToken)
        val edit = prefs.edit().putString("progress",data.toString())
        return edit.commit()
    }
}
