package com.tecsup.lab13animaciones.fishing

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class FishingLogicTest {
    @Test fun purchasesAreAffordableUniqueAndEquippable() {
        val initial=Progress(coins=500,xp=Progression.threshold(8))
        val bought=FishingLogic.buy(initial,2)
        assertEquals(100,bought.coins);assertEquals(2,bought.equipped);assertTrue(2 in bought.owned)
        assertEquals(bought,FishingLogic.buy(bought,2))
        assertEquals(bought,FishingLogic.buy(bought,5))
        assertEquals(bought,FishingLogic.equip(bought,5))
        assertEquals(0,FishingLogic.equip(bought,0).equipped)
        assertEquals(initial,FishingLogic.buy(initial,99))
    }
    @Test fun everyAccessibleDepthHasOnlyLegalFish() {
        for(rod in rods)for(zone in zones)for(depth in 0..501) {
            val pool=FishingLogic.eligible(zone,rod,depth)
            if(depth>zone.min && depth<=minOf(zone.max,rod.depth))assertTrue("Empty pool ${rod.name} ${zone.name} $depth",pool.isNotEmpty())
            else assertTrue(pool.isEmpty())
            pool.forEach { assertEquals(zone.id,it.zone);assertTrue(depth in it.min..it.max);assertTrue(depth<=rod.depth) }
        }
    }
    @Test fun weightedSelectionFavorsCommonSpecies() {
        val random=Random(20)
        val results=(1..10000).map { FishingLogic.select(zones[3],rods[5],450,random)!! }
        val common=results.count { it.rarity==Rarity.COMMON };val rare=results.count { it.rarity==Rarity.RARE };val legendary=results.count { it.rarity==Rarity.LEGENDARY }
        assertTrue(common>rare*4);assertTrue(rare>legendary*4);assertTrue(legendary>0)
        assertNull(FishingLogic.select(zones[3],rods[0],450,random))
    }
    @Test fun rewardsAreIdempotentAndKeepBestRecord() {
        val fish=species.first()
        val first=Catch("cast-1",fish,8,30)
        val p=FishingLogic.reward(Progress(),first)
        assertEquals(fish.value,p.coins);assertEquals(FishRecord(1,30),p.records[fish.id]);assertEquals(8,p.deepest)
        assertEquals(p,FishingLogic.reward(p,first))
        val second=FishingLogic.reward(p,first.copy(token="cast-2",depth=5,sizeCm=20))
        assertEquals(fish.value*2,second.coins);assertEquals(FishRecord(2,30),second.records[fish.id]);assertEquals(8,second.deepest)
    }
    @Test fun capturePrecisionAndEquipmentBonuses() {
        assertTrue(FishingLogic.hit(.5f,.5f,.2f));assertFalse(FishingLogic.hit(.1f,.5f,.2f))
        assertTrue(FishingLogic.targetWidth(species.first(),rods[0])>FishingLogic.targetWidth(species.last(),rods[0]))
        assertTrue(FishingLogic.targetWidth(species.last(),rods[5])>FishingLogic.targetWidth(species.last(),rods[0]))
        assertFalse(FishingLogic.available(zones[1],rods[1]));assertTrue(FishingLogic.available(zones[1],rods[2]))
    }
    @Test fun catalogIsConsistentAndExpandable() {
        assertEquals(12,species.size);assertEquals(species.size,species.map { it.id }.toSet().size)
        species.forEach { val z=zones[it.zone];assertTrue(it.min>z.min);assertTrue(it.max<=z.max);assertTrue(it.value>0) }
        assertEquals(listOf(0,150,400,900,1800,3500),rods.map { it.price })
        assertEquals(listOf(10,25,50,100,200,500),rods.map { it.depth })
    }
}
