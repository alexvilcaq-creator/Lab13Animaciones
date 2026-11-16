package com.tecsup.lab13animaciones.fishing

import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import java.util.UUID

enum class DivePhase { PREPARADO, LANZANDO, DESCENDIENDO, ENGANCHADO, RECOGIENDO, RESULTADO }
data class SwimFish(val id: Int, val fish: Fish, val x: Double, val depth: Double,
                    val velocity: Double, val length: Double, val specimenCm: Int = fish.sizeCm,
                    val anchorDepth: Double = depth, val turnAt: Double = 0.0,
                    val alertUntil: Double = 0.0, val restUntil: Double = 0.0,
                    val cruiseVelocity: Double = velocity) {
    val depthRadius: Double get() = .35 + length*.04
}
data class DiveControls(val left: Boolean = false, val right: Boolean = false, val reel: Boolean = false, val targetX: Double? = null)
data class DiveState(
    val phase: DivePhase = DivePhase.PREPARADO, val x: Double = 50.0, val depth: Double = 0.0,
    val time: Double = 0.0, val castTime: Double = 0.0, val tension: Double = 0.0,
    val overload: Double = 0.0, val fish: List<SwimFish> = emptyList(), val hooked: SwimFish? = null,
    val deepest: Double = 0.0, val hookDepth: Double = 0.0, val catch: Catch? = null, val message: String = "", val session: String = ""
) {
    val active get() = phase != DivePhase.PREPARADO && phase != DivePhase.RESULTADO
}

/** Pure world model. X is in world units (0..100); Y is depth in metres.
 * Fixed steps keep movement, swept collisions and overload duration independent of render FPS.
 * No Android state or rewards live in this engine. Recreation cancels an unfinished session.
 */
class FishingSimulation(val zone: Zone, val rod: Rod, private val random: Random = Random.Default,
                        private val initialPopulation: List<SwimFish>? = null, private var playerLevel: Int = 1) {
    val maxDepth = minOf(zone.max,rod.depth).toDouble()
    private var remainder = 0.0
    private val step = 1.0/120.0
    var state = DiveState(fish=populate())
        private set

    private fun populate(): List<SwimFish> {
        initialPopulation?.let { population ->
            require(population.map { it.id }.distinct().size==population.size)
            require(population.all { it.fish.zone==zone.id && it.depth>=it.fish.min && it.depth<=minOf(it.fish.max.toDouble(),maxDepth) && it.x in 0.0..100.0 })
            return population
        }
        var id=0
        return species.filter { FishingLogic.visible(it,zone,rod,playerLevel) }.flatMap { fish ->
            val count=when(fish.rarity) { Rarity.COMMON -> 5; Rarity.UNCOMMON -> 3; else -> 1 }
            val bottom=minOf(fish.max.toDouble(),maxDepth)
            (0 until count).map { n ->
                val d=fish.min+(bottom-fish.min)*(n+.35)/count
                val specimen=(fish.sizeCm*random.nextDouble(.8,1.3)).toInt().coerceAtLeast(1)
                val speed=random.nextDouble(4.0,9.0)*when(fish.behavior) {
                    FishBehavior.HEAVY -> .55
                    FishBehavior.FAST -> 1.4
                    else -> 1.0
                }
                SwimFish(id++,fish,random.nextDouble(5.0,95.0),d,
                    (if(random.nextBoolean())1 else -1)*speed,
                    (4.0+specimen/35.0).coerceAtMost(16.0),specimen)
            }
        }
    }
    fun launch(token: String = UUID.randomUUID().toString(), level: Int = playerLevel) {
        if(state.active) return
        playerLevel=level
        remainder=0.0
        state=DiveState(phase=DivePhase.LANZANDO,fish=populate(),session=token)
    }
    fun cancel() {
        remainder=0.0
        state=state.copy(phase=DivePhase.PREPARADO,hooked=null,catch=null,depth=0.0,tension=0.0,message="Sesión cancelada sin premios")
    }
    fun advance(seconds: Double, controls: DiveControls): DiveState {
        require(seconds.isFinite() && seconds>=0)
        remainder+=seconds
        while(remainder+1e-10>=step) {
            tick(controls)
            remainder=(remainder-step).coerceAtLeast(0.0)
        }
        return state
    }
    private fun tick(c: DiveControls) {
        val old=state
        val swimmers=old.fish.map { FishMotion.advance(it,old.x,old.depth,
            old.phase==DivePhase.DESCENDIENDO,old.time,step,maxDepth) }
        var s=old.copy(time=old.time+step,fish=swimmers)
        when(old.phase) {
            DivePhase.LANZANDO -> {
                val t=old.castTime+step
                s=s.copy(castTime=t,phase=if(t>=.9)DivePhase.DESCENDIENDO else DivePhase.LANZANDO)
            }
            DivePhase.DESCENDIENDO -> {
                val x=moveHook(old.x,c,28.0)
                val depth=(old.depth+(1.8+sqrt(rod.depth.toDouble())/2)*step).coerceAtMost(maxDepth)
                // Earliest contact wins, including relative fish motion between samples.
                val contact=swimmers.mapNotNull { f ->
                    val previous=old.fish.first { it.id==f.id }
                    sweptContact(old.x-previous.x,old.depth-previous.depth,x-f.x,depth-f.depth,f.length*.48+.8,f.depthRadius+.12)
                        ?.let { Triple(it,f,previous) }
                }.minByOrNull { it.first }
                s=s.copy(x=x,depth=depth,deepest=maxOf(old.deepest,depth))
                if(contact!=null) {
                    val (t,f,previous)=contact
                    val atDepth=old.depth+(depth-old.depth)*t
                    val atX=old.x+(x-old.x)*t
                    val hooked=f.copy(x=previous.x+(f.x-previous.x)*t,depth=atDepth)
                    s=s.copy(phase=DivePhase.ENGANCHADO,x=atX,depth=atDepth,hookDepth=atDepth,hooked=hooked,
                        fish=swimmers.filter { it.id!=f.id },tension=.2)
                }
            }
            DivePhase.ENGANCHADO, DivePhase.RECOGIENDO -> {
                val f=old.hooked ?: return
                val resistance=(f.fish.difficulty.toDouble()+f.fish.rarity.ordinal*.06+f.specimenCm/1800.0)/rod.resistance
                val tension=(old.tension+if(c.reel)(.23+resistance*.23)*step else -.38*step).coerceIn(0.0,1.0)
                val overload=if(tension>=1.0)old.overload+step else 0.0
                val depth=(old.depth+if(c.reel)-(3.0+rod.depth/9.0)*rod.reelSpeed*(1-resistance*.3).coerceAtLeast(.35)*step else (.35+resistance)*step).coerceIn(0.0,maxDepth)
                val guided=moveHook(old.x,c,20.0)
                val x=(guided+sin(old.time*4)*resistance*15*step).coerceIn(2.0,98.0)
                s=s.copy(phase=if(c.reel)DivePhase.RECOGIENDO else DivePhase.ENGANCHADO,
                    x=x,depth=depth,deepest=maxOf(old.deepest,depth),tension=tension,overload=overload,hooked=f.copy(x=x,depth=depth))
                if(overload>=1.2) s=s.copy(phase=DivePhase.RESULTADO,message="El hilo cedió. El pez escapó.",catch=null)
                else if(depth<=0 && c.reel) s=s.copy(phase=DivePhase.RESULTADO,message="¡Captura en superficie!",
                    catch=Catch(old.session,f.fish,old.hookDepth.toInt().coerceAtLeast(f.fish.min),
                        f.specimenCm))
            }
            else -> Unit
        }
        state=s
    }

    private fun moveHook(x: Double, controls: DiveControls, speed: Double): Double {
        val target=controls.targetX?.takeIf { it.isFinite() }?.coerceIn(2.0,98.0)
        val velocity=if(target!=null) ((target-x)*8*rod.control).coerceIn(-speed*rod.control,speed*rod.control)
            else ((if(controls.right)1 else 0)-(if(controls.left)1 else 0))*speed*rod.control
        return (x+velocity*step).coerceIn(2.0,98.0)
    }

    companion object {
        /** Segment against an ellipse; returned fraction is the first actual contact. */
        fun sweptContact(x0: Double, y0: Double, x1: Double, y1: Double, rx: Double, ry: Double): Double? {
            val x = x0 / rx
            val y = y0 / ry
            val dx = (x1 - x0) / rx
            val dy = (y1 - y0) / ry
            val c = x * x + y * y - 1
            if (c <= 0) {
                return 0.0
            }
            val a = dx * dx + dy * dy
            if (a < 1e-15) {
                return null
            }
            val b = 2 * (x * dx + y * dy)
            val discriminant = b * b - 4 * a * c
            if (discriminant < 0) {
                return null
            }
            return ((-b - sqrt(discriminant)) / (2 * a)).takeIf { it in 0.0..1.0 }
        }
    }
}
