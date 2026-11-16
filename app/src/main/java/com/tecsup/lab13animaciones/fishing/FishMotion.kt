package com.tecsup.lab13animaciones.fishing

import kotlin.math.abs
import kotlin.math.sin

/** Bounded, deterministic AI. Fear is a short burst followed by cooldown, never despawning a fish. */
object FishMotion {
    fun advance(f: SwimFish, hookX: Double, hookDepth: Double, hookActive: Boolean,
                time: Double, dt: Double, maxDepth: Double): SwimFish {
        var next=f
        var velocity=f.velocity
        var depth=f.depth
        when(f.fish.behavior) {
            FishBehavior.FAST -> {
                if(time>=f.turnAt) {
                    if(f.turnAt>0) velocity=-velocity
                    next=next.copy(turnAt=time+1.6+(f.id%3)*.35)
                }
            }
            FishBehavior.SHY -> {
                val dx=f.x-hookX
                val dy=f.depth-hookDepth
                val nearby=dx*dx/(14*14)+dy*dy/(4*4)<1
                if(hookActive && nearby && time>=f.restUntil) {
                    next=next.copy(alertUntil=time+.8,restUntil=time+3.6)
                }
                velocity=if(time<next.alertUntil) {
                    val direction=if(dx>=0)1 else -1
                    direction*maxOf(5.0,abs(f.cruiseVelocity)*1.6)
                } else {
                    (if(f.velocity>=0)1 else -1)*abs(f.cruiseVelocity)
                }
            }
            FishBehavior.LEGENDARY -> {
                depth=(f.anchorDepth+sin(time*2.2+f.id)*1.2)
                    .coerceIn(f.fish.min.toDouble(),minOf(f.fish.max.toDouble(),maxDepth))
            }
            else -> Unit
        }
        val dance=if(f.fish.behavior==FishBehavior.LEGENDARY)sin(time*2.8+f.id)*5 else 0.0
        var x=f.x+(velocity+dance)*dt
        if(x<0) {
            x=-x
            velocity=abs(velocity)
        } else if(x>100) {
            x=200-x
            velocity=-abs(velocity)
        }
        return next.copy(x=x.coerceIn(0.0,100.0),depth=depth,velocity=velocity)
    }
}
