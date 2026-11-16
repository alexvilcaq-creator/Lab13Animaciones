package com.tecsup.lab13animaciones.fishing

import kotlin.math.abs
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class FishingExpansionTest {
    private val perca=species.first { it.id=="perca" }
    private fun motion(behavior: FishBehavior, x: Double=55.0, depth: Double=6.0)=
        SwimFish(1,perca.copy(behavior=behavior),x,depth,5.0,6.0)

    @Test fun dragSmoothlyFollowsTargetWithSpeedAndWorldLimits() {
        val e=FishingSimulation(zones[0],rods[0],Random(1),emptyList())
        e.launch("drag");e.advance(1.0,DiveControls())
        val start=e.state.x
        e.advance(.1,DiveControls(targetX=98.0))
        assertTrue(e.state.x>start)
        assertTrue(e.state.x-start<=28*.1+1e-8)
        e.advance(5.0,DiveControls(targetX=10000.0))
        assertEquals(98.0,e.state.x,1e-5)
        e.advance(10.0,DiveControls(targetX=-10000.0))
        assertEquals(2.0,e.state.x,1e-5)
        val released=e.state.x
        e.advance(.5,DiveControls())
        assertEquals(released,e.state.x,1e-8)
        assertEquals(10.0,e.state.depth,1e-8)
    }
    @Test fun dragAndAiAreIndependentOfRenderFrameRate() {
        fun run(dt: Double,n: Int): DiveState {
            val e=FishingSimulation(zones[0],rods[0],Random(4),playerLevel=30)
            e.launch("same")
            repeat(n) { e.advance(dt,DiveControls(targetX=85.0)) }
            return e.state
        }
        val fast=run(1.0/120,600)
        val slow=run(.5,10)
        assertEquals(fast.phase,slow.phase)
        assertEquals(fast.x,slow.x,1e-8)
        assertEquals(fast.depth,slow.depth,1e-8)
        assertEquals(fast.fish,slow.fish)
    }
    @Test fun calmFishDoNotFleeAndFastFishTurn() {
        val calm=motion(FishBehavior.CALM)
        val moved=FishMotion.advance(calm,54.0,6.0,true,1.0,.1,10.0)
        assertEquals(calm.velocity,moved.velocity,0.0)
        assertEquals(calm.x+.5,moved.x,1e-8)
        val fast=motion(FishBehavior.FAST).copy(turnAt=1.0)
        val turned=FishMotion.advance(fast,0.0,0.0,false,1.01,.01,10.0)
        assertTrue(turned.velocity<0)
    }
    @Test fun fearHasProximityDetectionShortBurstAndCooldown() {
        val shy=motion(FishBehavior.SHY)
        val near=FishMotion.advance(shy,50.0,6.0,true,1.0,.01,10.0)
        assertTrue(near.velocity>shy.velocity)
        assertTrue(near.alertUntil>1.0)
        val resting=FishMotion.advance(near,near.x-1,6.0,true,2.0,.01,10.0)
        assertEquals(near.alertUntil,resting.alertUntil,0.0)
        assertEquals(abs(shy.velocity),abs(resting.velocity),1e-8)
        val far=FishMotion.advance(shy,0.0,0.0,true,1.0,.01,10.0)
        assertEquals(0.0,far.alertUntil,0.0)
        val ignored=FishMotion.advance(shy,50.0,6.0,false,1.0,.01,10.0)
        assertEquals(0.0,ignored.alertUntil,0.0)
    }
    @Test fun frightenedFishRemainCatchableAtContact() {
        val shy=motion(FishBehavior.SHY,x=50.0,depth=3.0).copy(velocity=0.0,cruiseVelocity=0.0)
        val e=FishingSimulation(zones[0],rods[0],Random(1),listOf(shy))
        e.launch("chase")
        repeat(1200) {
            val target=e.state.fish.firstOrNull()?.x ?: e.state.x
            e.advance(1.0/120,DiveControls(targetX=target))
        }
        assertNotNull(e.state.hooked)
        assertEquals(1,e.state.hooked!!.id)
    }
    @Test fun legendaryFishDanceWithinLegalDepthAndWorldBounds() {
        val f=species.first { it.id=="fenix_marea" }
        var swimmer=SwimFish(1,f,99.9,175.1,8.0,12.0)
        var changed=false
        repeat(1200) { n ->
            swimmer=FishMotion.advance(swimmer,50.0,180.0,true,n/120.0,1.0/120,200.0)
            assertTrue(swimmer.x in 0.0..100.0)
            assertTrue(swimmer.depth in 175.0..200.0)
            changed=changed || abs(swimmer.depth-175.1)>.1
        }
        assertTrue(changed)
    }
    @Test fun secretsRespectLevelZoneAndRodReachAndUnlockOnNextLaunch() {
        val secret=species.first { it.special==FishSpecial.SECRET }
        assertFalse(FishingLogic.visible(secret,zones[1],rods[2],7))
        assertFalse(FishingLogic.visible(secret,zones[0],rods[2],8))
        assertFalse(FishingLogic.visible(secret,zones[1],rods[1],8))
        assertTrue(FishingLogic.visible(secret,zones[1],rods[2],8))
        val e=FishingSimulation(zones[1],rods[2],Random(3),playerLevel=7)
        assertTrue(e.state.fish.none { it.fish.id==secret.id })
        e.launch(level=8)
        assertTrue(e.state.fish.any { it.fish.id==secret.id })
        e.state.fish.forEach { assertTrue(it.depth<=50) }
    }
    @Test fun specialRewardsAndMissionsPayOnceAndSurviveRoundTrip() {
        val gold=species.first { it.special==FishSpecial.GOLD }
        val crystal=species.first { it.special==FishSpecial.CRYSTAL }
        assertEquals(gold.value*2,FishingLogic.catchCoins(gold))
        assertEquals(Progression.catchXp(crystal.rarity)*3,FishingLogic.catchXp(crystal))
        val legendary=species.first { it.id=="fenix_marea" }
        assertTrue(FishingLogic.catchCoins(legendary)>legendary.value)
        assertTrue(FishingLogic.catchXp(legendary)>Progression.catchXp(legendary.rarity))
        var p=Progress()
        repeat(5) { p=FishingLogic.reward(p,Catch("mission-$it",perca,5,18+it)) }
        assertEquals(5,p.missionCatches)
        assertEquals(5*perca.value+100,p.coins)
        assertEquals(5*25+75,p.xp)
        assertEquals(setOf("primeros_cinco"),p.completedMissions)
        p=FishingLogic.explore(p,40)
        assertTrue("explorador_40" in p.completedMissions)
        val restored=FishingSaveCodec.decode(FishingSaveCodec.encode(p))
        assertEquals(p,restored)
        assertEquals(p,FishingLogic.explore(restored,40))
        assertEquals(p,FishingLogic.reward(restored,Catch("mission-0",perca,5,18)))
        assertEquals(p,FishingMissions.settle(restored))
    }
    @Test fun migrationDoesNotAutoPayMissionsOrLoseLegacyProgress() {
        val p=FishingSaveCodec.decode("""{"version":2,"coins":700,"xp":2400,"owned":"0,3","equipped":3,"deepest":87,"records":{"perca":{"count":100,"best":32}}}""")
        assertEquals(700,p.coins)
        assertEquals(2400,p.xp)
        assertEquals(3,p.equipped)
        assertEquals(87,p.deepest)
        assertEquals(100,p.records["perca"]!!.count)
        assertEquals(0,p.missionCatches)
        assertTrue(p.completedMissions.isEmpty())
        assertEquals(p,FishingMissions.settle(p))
        assertEquals(p,FishingSaveCodec.decode(FishingSaveCodec.encode(p)))
    }
    @Test fun sixRodsKeepReachAndActuallyChangeHandlingAndReeling() {
        assertEquals(listOf(10,25,50,100,200,500),rods.map { it.depth })
        rods.zipWithNext().forEach { (a,b) ->
            assertTrue(b.control>a.control)
            assertTrue(b.resistance>a.resistance)
            assertTrue(b.reelSpeed>a.reelSpeed)
        }
        val low=Progress(owned=setOf(0,5))
        assertEquals(low,FishingLogic.equip(low,5))
        assertEquals(5,FishingLogic.equip(low.copy(xp=Progression.threshold(30)),5).equipped)
        fun held(rod: Rod): DiveState {
            val e=FishingSimulation(zones[0],rod,Random(1),listOf(SwimFish(0,perca,50.0,8.0,0.0,6.0)))
            e.launch("rod")
            repeat(1000) { if(e.state.hooked==null)e.advance(.01,DiveControls()) }
            assertNotNull(e.state.hooked)
            e.advance(.1,DiveControls(reel=true))
            return e.state
        }
        val wood=held(rods[0]);val better=held(rods[1])
        assertTrue(better.tension<wood.tension)
        assertTrue(better.depth<wood.depth)
    }
    @Test fun diversityMissionAndVariableSizesHavePersistentEvidence() {
        var p=Progress()
        listOf("perca","koi_sol","tetra_cristal").forEachIndexed { i,id ->
            p=FishingLogic.reward(p,Catch("diverse-$i",species.first { it.id==id },8,30+i))
        }
        assertTrue("tres_especies" in p.completedMissions)
        assertEquals(3,p.missionSpecies.size)
        assertEquals(3,p.records.size)
        assertEquals(p,FishingMissions.settle(FishingSaveCodec.decode(FishingSaveCodec.encode(p))))
        val e=FishingSimulation(zones[1],rods[3],Random(7),playerLevel=12)
        assertTrue(e.state.fish.any { it.fish.behavior==FishBehavior.HEAVY })
        e.state.fish.forEach { f ->
            assertTrue(f.specimenCm>0)
            assertTrue(f.specimenCm<=(f.fish.sizeCm*1.3).toInt())
            if(f.fish.behavior==FishBehavior.HEAVY) assertTrue(abs(f.velocity)<=9*.55)
        }
        assertTrue(e.state.fish.filter { it.fish.id=="bagre" }.map { it.specimenCm }.distinct().size>1)
    }

}
