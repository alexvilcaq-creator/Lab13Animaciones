package com.tecsup.lab13animaciones.fishing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.floor
import kotlin.math.sin

/** Draws the exact fish positions and collision dimensions exposed by the engine. */
@Composable fun DiveScene(zone: Zone, rod: Rod, state: DiveState, modifier: Modifier=Modifier) {
    Box(modifier.clipToBounds()) {
        FishingScene(zone,rod,if(state.phase==DivePhase.LANZANDO)FishingPhase.CASTING else FishingPhase.IDLE,
            (state.castTime/.9).toFloat().coerceIn(0f,1f),0f,0f,null,Modifier.fillMaxSize(),showFish=false,externalTime=state.time.toFloat())
        Canvas(Modifier.fillMaxSize().semantics { contentDescription="Mundo submarino. Anzuelo a ${state.depth.toInt()} metros" }) {
            val w=size.width; val h=size.height; val surface=h*.32f; val waterHeight=h-surface
            // 18 visible metres at any depth: the boat remains in a surface inset above the scrolling world.
            val span=18.0;val camera=(state.depth-span*.48).coerceAtLeast(0.0)
            fun y(depth: Double)=surface+((depth-camera)/span*waterHeight).toFloat()
            fun point(x: Double,depth: Double)=Offset((x/100*w).toFloat(),y(depth))
            val darkness=(camera/500).toFloat().coerceIn(0f,1f)
            val top=androidx.compose.ui.graphics.lerp(Color(zone.water),Color(0xFF080E28),darkness)
            drawRect(Brush.verticalGradient(listOf(top,Color(0xFF061426)),surface,h),Offset(0f,surface),Size(w,waterHeight))
            val first=floor(camera/5).toInt()*5
            for(d in first..(camera+span).toInt() step 5) {
                val yy=y(d.toDouble())
                if(yy>=surface) {
                    drawLine(Color.White.copy(.12f),Offset(0f,yy),Offset(w,yy),1f)
                    repeat(4) { i -> drawLine(Color.White.copy(.3f),Offset(i*w/3,yy),Offset(i*w/3+7f,yy),2f) }
                }
            }
            // Persistent world decorations, rather than a seabed attached to the camera.
            val start=floor(camera/6).toInt()
            for(i in start..start+4) {
                val depth=i*6.0+4
                val xx=if(i%2==0)w*.07f else w*.93f;val yy=y(depth)
                if(yy in surface..h) {
                    drawOval(Color(0xFF345267),Offset(xx-w*.1f,yy),Size(w*.2f,w*.1f))
                    repeat(3) { j ->
                        val sway=sin(state.time*1.5+i+j).toFloat()*w*.018f
                        val p=Path().apply { moveTo(xx+j*5,yy+8);cubicTo(xx-15,yy-12,xx+sway+15,yy-28,xx+sway,yy-45-j*7) }
                        drawPath(p,Color(0xFF4B9C88),style=Stroke(4f,cap=StrokeCap.Round))
                    }
                }
            }
            repeat(18) { i ->
                val xx=((i*.137+sin(state.time*.5+i)*.01)%1*w).toFloat()
                val yy=surface+(((i*.071-state.time*.035)%1+1)%1*waterHeight).toFloat()
                drawCircle(Color(0xFFBCF8ED).copy(.25f),2f+i%3,Offset(xx,yy),style=Stroke(1.5f))
            }
            fun drawSwimmer(f: SwimFish, at: Offset, direction: Float) {
                val length=(f.length/100*w).toFloat()
                val shapeHeight=when(f.fish.shape) { 1 -> .31f; 2 -> .65f; 3 -> .38f; else -> .5f }
                val scaleY=(2*f.depthRadius/span*waterHeight/(length*shapeHeight)).toFloat()
                withTransform({ scale(1f,scaleY,at) }) {
                    drawFish(f.fish,at,length,direction,state.time.toFloat()*5)
                }
            }
            state.fish.forEach { f ->
                val yy=y(f.depth)
                if(yy>surface+w*.02 && yy<h+w*.08) drawSwimmer(f,point(f.x,f.depth),if(f.velocity>0)1f else -1f)
            }
            if(state.active && state.phase!=DivePhase.LANZANDO) {
                val hook=point(state.x,state.depth)
                drawLine(Color(0xFFE6F8E8),Offset(w*.52f,h*.06f),hook,2f)
                drawArc(Color.White,0f,275f,false,hook-Offset(4f,0f),Size(8f,12f),style=Stroke(3f))
                state.hooked?.let { f -> drawSwimmer(f,hook+Offset((f.length/100*w*.43).toFloat(),0f),-1f) }
                if(state.depth<.6) drawOval(Color.White.copy(.6f),Offset(hook.x-14,surface-3),Size(28f,6f),style=Stroke(2f))
            }
        }
    }
}
