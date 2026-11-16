package com.tecsup.lab13animaciones.fishing

import kotlin.math.abs
import kotlin.math.sin

/** Delayed reactions and bounded acceleration keep fish approachable and motion continuous. */
object FishMotion {
    fun advance(f: SwimFish, hookX: Double, hookDepth: Double, hookActive: Boolean,
                time: Double, dt: Double, maxDepth: Double): SwimFish {
        var next=f
        var target=f.desiredVelocity
        var depth=f.depth
        val special=f.fish.special==FishSpecial.GOLD || f.fish.special==FishSpecial.SECRET
        if(f.fish.behavior==FishBehavior.SHY || special) {
            val dx=f.x-hookX
            val dy=f.depth-hookDepth
            val nearby=dx*dx/(16*16)+dy*dy/(4.5*4.5)<1
            if(f.reactionAt>0 && (!nearby || !hookActive)) {
                next=next.copy(reactionAt=0.0,restUntil=time+.75)
            } else if(f.reactionAt>0 && time>=f.reactionAt) {
                next=next.copy(reactionAt=0.0,alertUntil=time+if(special).85 else .65,
                    restUntil=time+4.8,flightDirection=if(dx>=0)1.0 else -1.0)
            } else if(hookActive && nearby && time>=f.restUntil && f.reactionAt==0.0) {
                next=next.copy(reactionAt=time+if(special).4 else .55)
            }
            target=if(time<next.alertUntil) {
                next.flightDirection*(abs(f.cruiseVelocity)*1.15).coerceIn(3.0,if(special)6.5 else 5.5)
            } else {
                (if(f.desiredVelocity>=0)1 else -1)*abs(f.cruiseVelocity)
            }
        } else if(f.fish.behavior==FishBehavior.FAST) {
            if(time>=f.turnAt) {
                if(f.turnAt>0) target=-f.desiredVelocity
                next=next.copy(turnAt=time+3.5+(f.id%3)*.75)
            }
        } else if(f.fish.behavior==FishBehavior.LEGENDARY) {
            val desiredDepth=(f.anchorDepth+sin(time*1.6+f.id)*1.0)
                .coerceIn(f.fish.min.toDouble(),minOf(f.fish.max.toDouble(),maxDepth))
            depth=f.depth+(desiredDepth-f.depth).coerceIn(-.55*dt,.55*dt)
            target=(if(f.desiredVelocity>=0)1 else -1)*abs(f.cruiseVelocity)+sin(time*2+f.id)*2
        }
        // Turn before the wall. Never reflect position to another point in the world.
        if(f.x<6) target=abs(f.cruiseVelocity)
        if(f.x>94) target=-abs(f.cruiseVelocity)
        val velocity=f.velocity+(target-f.velocity).coerceIn(-5*dt,5*dt)
        val x=(f.x+velocity*dt).coerceIn(0.0,100.0)
        val stopped=(x==0.0 && velocity<0) || (x==100.0 && velocity>0)
        return next.copy(x=x,depth=depth,velocity=if(stopped)0.0 else velocity,desiredVelocity=target)
    }
}
