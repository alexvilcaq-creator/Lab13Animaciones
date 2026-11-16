package com.tecsup.lab13animaciones.fishing

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class FishingSimulationTest {
    private fun empty()=FishingSimulation(zones[0],rods[0],Random(1),emptyList())
    private fun engine(fish: Fish=species[0],depth: Double=3.0,rod: Rod=rods[0]) =
        FishingSimulation(zones[fish.zone],rod,Random(1),listOf(SwimFish(0,fish,50.0,depth,0.0,6.0)))
    private fun hook(e: FishingSimulation) {
        e.launch("session")
        repeat(5000) {
            if(e.state.hooked!=null) return
            e.advance(.05,DiveControls())
        }
        fail("No contact")
    }
    @Test fun depthAndHorizontalLimitsAndOpposingControls() {
        val e=empty();e.launch();e.advance(1.0,DiveControls())
        val start=e.state.x
        e.advance(1.0,DiveControls(left=true,right=true));assertEquals(start,e.state.x,1e-8)
        e.advance(10.0,DiveControls(left=true));assertEquals(2.0,e.state.x,1e-8)
        assertEquals(10.0,e.state.depth,1e-8)
        e.advance(10.0,DiveControls(right=true));assertEquals(98.0,e.state.x,1e-8)
        assertTrue(e.state.depth<=e.maxDepth)
        e.cancel();assertEquals(DivePhase.PREPARADO,e.state.phase);assertNull(e.state.catch)
    }
    @Test fun sweptCollisionCatchesCrossingAndRejectsMisses() {
        assertNotNull(FishingSimulation.sweptContact(-20.0,0.0,20.0,0.0,2.0,.5))
        assertNotNull(FishingSimulation.sweptContact(0.0,-20.0,0.0,20.0,2.0,.5))
        assertNull(FishingSimulation.sweptContact(-20.0,2.0,20.0,2.0,2.0,.5))
        assertEquals(0.0,FishingSimulation.sweptContact(0.0,0.0,5.0,5.0,2.0,.5)!!,0.0)
    }
    @Test fun contactHooksOnlyOneAndStopsDescent() {
        val f=species[0]
        val e=FishingSimulation(zones[0],rods[0],Random(1),listOf(
            SwimFish(1,f,50.0,3.0,0.0,6.0),SwimFish(2,f,50.0,3.0,0.0,6.0)))
        hook(e)
        assertEquals(1,e.state.hooked!!.id);assertEquals(1,e.state.fish.size)
        val session=e.state.session;e.launch("duplicate-launch");assertEquals(session,e.state.session)
        val depth=e.state.depth
        e.advance(.2,DiveControls(reel=true));assertTrue(e.state.depth<depth)
        assertEquals(1,e.state.hooked!!.id)
    }
    @Test fun holdingRaisesTensionAndReleasingRelievesIt() {
        val e=engine(depth=8.0);hook(e)
        e.advance(.3,DiveControls(reel=true));val held=e.state
        assertTrue(held.tension>.2)
        e.advance(.2,DiveControls());assertTrue(e.state.tension<held.tension)
        assertTrue(e.state.depth>held.depth);assertEquals(DivePhase.ENGANCHADO,e.state.phase)
    }
    @Test fun captureCompletesOnceAndKeepsOriginalHookDepth() {
        val e=engine(depth=5.0);hook(e)
        e.advance(4.0,DiveControls(reel=true))
        assertEquals(DivePhase.RESULTADO,e.state.phase)
        val caught=e.state.catch!!;assertEquals("session",caught.token);assertTrue(caught.depth>=4)
        e.advance(10.0,DiveControls(reel=true));assertEquals(caught,e.state.catch)
        val first=FishingLogic.reward(Progress(),caught)
        val later=FishingLogic.reward(first,caught.copy(token="other"))
        assertEquals(later,FishingLogic.reward(later,caught))
    }
    @Test fun sustainedOverloadEscapesAndControlledReelingCanLandLegendaryFish() {
        val escape=engine(species.first { it.id=="leviatan" },440.0,rods.last());hook(escape)
        escape.advance(5.0,DiveControls(reel=true))
        assertEquals(DivePhase.DETENIDO,escape.state.phase);assertNull(escape.state.catch)
        assertNull(escape.state.hooked)
        escape.recall();escape.advance(30.0,DiveControls())
        assertEquals(DivePhase.RESULTADO,escape.state.phase)
        assertEquals(0.0,escape.state.depth,0.0)
        val land=engine(species.first { it.id=="leviatan" },440.0,rods.last());hook(land)
        var holding=true
        repeat(5000) {
            if(land.state.tension>.8) holding=false
            if(land.state.tension<.3) holding=true
            land.advance(.02,DiveControls(reel=holding))
        }
        assertNotNull(land.state.catch);assertEquals(0.0,land.state.depth,1e-8)
    }
    @Test fun simulationIsIndependentOfFrameRateIncludingSlowFrames() {
        fun run(dt: Double,n: Int): DiveState {
            val e=engine(depth=5.0);e.launch("same")
            repeat(n) { e.advance(dt,DiveControls()) }
            return e.state
        }
        val fast=run(1.0/120,720);val medium=run(1.0/30,180);val slow=run(.5,12)
        assertEquals(fast.phase,slow.phase);assertEquals(fast.hooked,slow.hooked)
        assertEquals(fast.depth,slow.depth,1e-8);assertEquals(fast.depth,medium.depth,1e-8)
        assertEquals(fast.x,slow.x,1e-8)
        fun struggle(dt: Double,n: Int): DiveState {
            val e=engine(species.first { it.id=="leviatan" },440.0,rods.last());hook(e)
            repeat(n) { e.advance(dt,DiveControls(reel=true)) };return e.state
        }
        val a=struggle(1.0/120,240);val b=struggle(.5,4)
        assertEquals(a.depth,b.depth,1e-8);assertEquals(a.tension,b.tension,1e-8)
        assertEquals(a.phase,b.phase)
    }
    @Test fun fishPopulationRespectsSpeciesZoneDepthAndSize() {
        for(z in zones) {
            val e=FishingSimulation(z,rods.last(),Random(8))
            assertTrue(e.state.fish.isNotEmpty())
            e.state.fish.forEach { assertEquals(z.id,it.fish.zone);assertTrue(it.depth>=it.fish.min && it.depth<=it.fish.max) }
        }
        val e=engine();hook(e);e.cancel();e.advance(2.0,DiveControls(reel=true));assertNull(e.state.catch)
        assertEquals(DivePhase.PREPARADO,e.state.phase)
    }
}
