package com.tecsup.lab13animaciones.fishing

import kotlin.random.Random
import kotlin.math.abs

object FishingLogic {
    fun available(zone: Zone, rod: Rod) = rod.depth > zone.min
    fun eligible(zone: Zone, rod: Rod, depth: Int): List<Fish> =
        if (depth <= zone.min || depth > minOf(zone.max, rod.depth)) emptyList()
        else species.filter { it.zone == zone.id && depth in it.min..it.max }
    fun select(zone: Zone, rod: Rod, depth: Int, random: Random = Random.Default): Fish? {
        val pool = eligible(zone, rod, depth)
        if (pool.isEmpty()) return null
        var ticket = random.nextInt(pool.sumOf { it.rarity.weight })
        return pool.first { ticket -= it.rarity.weight; ticket < 0 }
    }
    fun buy(p: Progress, id: Int): Progress {
        val rod = rods.firstOrNull { it.id == id } ?: return p
        if (id in p.owned || p.coins < rod.price) return p
        return p.copy(coins = p.coins - rod.price, owned = p.owned + id, equipped = id)
    }
    fun equip(p: Progress, id: Int) = if (id in p.owned) p.copy(equipped = id) else p
    fun targetWidth(fish: Fish, rod: Rod) = (.36f - fish.difficulty * .24f + rod.id * .012f).coerceIn(.14f,.42f)
    fun hit(position: Float, center: Float, width: Float) = abs(position - center) <= width / 2
    fun reward(p: Progress, catch: Catch): Progress {
        if (p.lastRewardToken == catch.token) return p
        val old = p.records[catch.fish.id] ?: FishRecord()
        return p.copy(lastRewardToken = catch.token, coins = p.coins + catch.fish.value, deepest = maxOf(p.deepest, catch.depth),
            records = p.records + (catch.fish.id to FishRecord(old.count + 1, maxOf(old.bestCm, catch.sizeCm))))
    }
}
