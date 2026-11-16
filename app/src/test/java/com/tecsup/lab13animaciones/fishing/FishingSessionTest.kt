package com.tecsup.lab13animaciones.fishing

import kotlin.math.abs
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class FishingSessionTest {
    private val perca=species.first { it.id=="perca" }
    private fun empty(rod: Rod=rods[0])=FishingSimulation(zones[0],rod,Random(9),emptyList())

    @Test fun fullEmptySessionCanStopResumeExploreAtLimitAndReturnToBoat() {
        val e=empty()
        e.launch("empty");e.advance(1.3,DiveControls())
        assertEquals(DivePhase.DESCENDIENDO,e.state.phase)
        e.pauseDescent()
        val depth=e.state.depth
        val x=e.state.x
        e.advance(2.0,DiveControls(targetX=80.0))
        assertEquals(depth,e.state.depth,1e-8)
        assertTrue(e.state.x>x)
        e.resumeDescent();e.advance(6.0,DiveControls())
        assertEquals(10.0,e.state.depth,1e-8)
        assertEquals(DivePhase.DETENIDO,e.state.phase)
        e.resumeDescent();assertEquals(DivePhase.DETENIDO,e.state.phase)
        e.advance(1.0,DiveControls(targetX=20.0))
        assertTrue(e.state.x<80)
        e.recall();assertEquals(DivePhase.REGRESANDO_VACIO,e.state.phase)
        e.advance(10.0,DiveControls())
        assertEquals(DivePhase.RESULTADO,e.state.phase)
        assertEquals(0.0,e.state.depth,0.0)
        assertNull(e.state.catch)
        e.launch("again");assertEquals(DivePhase.LANZANDO,e.state.phase)
    }
    @Test fun emptyRecallDoesNotHookFishOnTheWayUpOrGenerateRewards() {
        val e=FishingSimulation(zones[0],rods[0],Random(1),
            listOf(SwimFish(0,perca,30.0,3.0,0.0,6.0)))
        e.launch("empty");e.advance(2.5,DiveControls())
        assertNull(e.state.hooked)
        e.recall()
        e.advance(10.0,DiveControls(targetX=10.0,reel=true))
        assertEquals(DivePhase.RESULTADO,e.state.phase)
        assertNull(e.state.catch);assertNull(e.state.hooked)
    }
    @Test fun pausedExplorationStillDetectsFishContactThenCanCompleteCapture() {
        val e=FishingSimulation(zones[0],rods[0],Random(1),
            listOf(SwimFish(0,perca,80.0,3.0,0.0,6.0)))
        e.launch("paused")
        e.advance(1.8,DiveControls())
        e.pauseDescent()
        e.advance(2.0,DiveControls(targetX=80.0))
        assertNotNull(e.state.hooked)
        val depth=e.state.depth
        e.recall() // Empty recall cannot bypass the tension game or discard a hooked fish.
        assertEquals(DivePhase.ENGANCHADO,e.state.phase)
        e.advance(.2,DiveControls(reel=true))
        assertTrue(e.state.depth<depth)
        e.advance(5.0,DiveControls(reel=true))
        assertEquals(DivePhase.RESULTADO,e.state.phase)
        val caught=e.state.catch!!
        val paid=FishingLogic.reward(Progress(),caught)
        assertEquals(paid,FishingLogic.reward(paid,caught))
    }
    @Test fun rareReactionIsDelayedAndMotionNeverJumpsOrReversesInstantly() {
        val fish=perca.copy(behavior=FishBehavior.SHY,rarity=Rarity.RARE)
        var f=SwimFish(0,fish,55.0,5.0,3.5,6.0)
        f=FishMotion.advance(f,50.0,5.0,true,1.0,.01,10.0)
        assertTrue(f.reactionAt>=1.5)
        assertEquals(0.0,f.alertUntil,0.0)
        val before=f
        f=FishMotion.advance(f,50.0,5.0,true,1.6,.01,10.0)
        assertTrue(f.alertUntil>1.6)
        assertTrue(abs(f.velocity-before.velocity)<=.05+1e-8)
        assertTrue(abs(f.x-before.x)<=.06)
        val fast=perca.copy(behavior=FishBehavior.FAST)
        val cruising=SwimFish(1,fast,50.0,5.0,4.0,6.0,turnAt=1.0)
        val turning=FishMotion.advance(cruising,0.0,0.0,false,1.1,.01,10.0)
        assertTrue(turning.desiredVelocity<0)
        assertTrue(turning.velocity>0)
    }
    @Test fun populationsAreSlowerAndSpecialFishRemainBelowPlayerSpeed() {
        for(zone in zones) {
            val e=FishingSimulation(zone,rods.last(),Random(4),playerLevel=30)
            e.state.fish.forEach { f ->
                assertTrue(abs(f.velocity)<=4.8)
                if(f.fish.rarity==Rarity.COMMON) assertTrue(abs(f.velocity)<=3.2)
            }
        }
        val special=species.first { it.special==FishSpecial.SECRET }
        var f=SwimFish(0,special,55.0,45.0,4.0,8.0)
        repeat(300) { n -> f=FishMotion.advance(f,50.0,45.0,true,1+n/120.0,1.0/120,100.0) }
        assertTrue(abs(f.velocity)<7.0)
        assertTrue(f.restUntil>0)
    }
    @Test fun fairCollisionIncludesHookTipAndRejectsActualMiss() {
        assertNotNull(FishingSimulation.sweptContact(-15.0,.6,15.0,.6,4.0,.75))
        assertNull(FishingSimulation.sweptContact(-15.0,1.5,15.0,1.5,4.0,.75))
    }
    @Test fun pausedAndEmptyReturnAreIndependentOfRenderFrameRate() {
        fun run(dt: Double,n: Int): DiveState {
            val e=empty()
            e.launch("fps");e.advance(2.0,DiveControls())
            e.pauseDescent();repeat(n) { e.advance(dt,DiveControls(targetX=80.0)) }
            e.recall();repeat(n) { e.advance(dt,DiveControls()) }
            return e.state
        }
        val fast=run(1.0/120,600)
        val slow=run(.5,10)
        assertEquals(fast.phase,slow.phase)
        assertEquals(fast.depth,slow.depth,1e-8)
        assertEquals(fast.x,slow.x,1e-8)
        assertNull(slow.catch)
    }
}
