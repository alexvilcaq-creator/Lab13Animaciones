package com.tecsup.lab13animaciones.fishing

import org.junit.Assert.*
import org.junit.Test

class FishingProgressTest {
    @Test fun thresholdsAndXpArePredictable() {
        assertEquals(1,Progression.level(0));assertEquals(100,Progression.threshold(2))
        for(level in 2..100) {
            assertEquals(level-1,Progression.level(Progression.threshold(level)-1))
            assertEquals(level,Progression.level(Progression.threshold(level)))
        }
        assertTrue(Progression.catchXp(Rarity.LEGENDARY)>Progression.catchXp(Rarity.RARE))
        val p=FishingLogic.reward(Progress(),Catch("xp",species[0],2,18))
        assertEquals(25,p.xp);assertEquals(p,FishingLogic.reward(p,Catch("xp",species[0],2,18)))
    }
    @Test fun purchasesRequireLevelAndCoinsButOldEquipmentRemainsUsable() {
        for(rod in rods.drop(1)) {
            val enough=Progress(coins=10000,xp=Progression.threshold(Progression.rodLevel(rod.id)))
            assertTrue(rod.id in FishingLogic.buy(enough,rod.id).owned)
            assertEquals(enough.copy(coins=0),FishingLogic.buy(enough.copy(coins=0),rod.id))
            val low=enough.copy(xp=0);assertEquals(low,FishingLogic.buy(low,rod.id))
        }
        val legacy=Progress(owned=setOf(0,5),equipped=5)
        assertEquals(5,FishingLogic.equip(legacy,5).equipped)
        for(z in zones) {
            val p=legacy.copy(xp=Progression.threshold(Progression.zoneLevel(z.id)))
            assertTrue(FishingLogic.available(z,p))
            if(z.id>0) {
                assertFalse(FishingLogic.available(z,legacy))
                assertFalse(FishingLogic.available(z,p.copy(equipped=0)))
            }
        }
        assertEquals(listOf(1,3,8,12,20,30),rods.map { Progression.rodLevel(it.id) })
        assertEquals(listOf(1,5,15,25),zones.map { Progression.zoneLevel(it.id) })
    }
    @Test fun v1MigrationPreservesProgressAndSeedsReceiptWithoutInventingXp() {
        val raw="""{"coins":1240,"owned":"0,2,5","equipped":5,"deepest":475,"reward_token":"old","records":{"leviatan":{"count":3,"best":390}}}"""
        val old=FishingSaveCodec.decode(raw)
        assertEquals(1240,old.coins);assertEquals(setOf(0,2,5),old.owned);assertEquals(5,old.equipped)
        assertEquals(475,old.deepest);assertEquals(FishRecord(3,390),old.records["leviatan"])
        assertEquals(0,old.xp);assertEquals(1,old.level);assertTrue("old" in old.rewardTokens)
        assertEquals(old,FishingSaveCodec.decode(FishingSaveCodec.encode(old)))
        val awarded=FishingLogic.reward(old,Catch("new",species.first { it.id=="leviatan" },450,410))
        val restored=FishingSaveCodec.decode(FishingSaveCodec.encode(awarded))
        assertEquals(awarded,restored);assertEquals(FishingLogic.catchXp(species.first { it.id=="leviatan" })+150,restored.xp)
        assertEquals(restored,FishingLogic.reward(restored,Catch("old",species.first { it.id=="leviatan" },450,410)))
        assertEquals(restored,FishingLogic.reward(restored,Catch("new",species.first { it.id=="leviatan" },450,410)))
    }
    @Test fun invalidFieldsAreSanitizedWithoutErasingOwnedEquipment() {
        val p=FishingSaveCodec.decode("""{"coins":-1,"owned":"0,4,99","equipped":99,"xp":-3,"deepest":999}""")
        assertEquals(0,p.coins);assertEquals(setOf(0,4),p.owned);assertEquals(0,p.equipped)
        assertEquals(0,p.xp);assertEquals(500,p.deepest)
        assertEquals(Progress(),FishingSaveCodec.decode("broken"))
    }
}
