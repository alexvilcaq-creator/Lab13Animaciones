package com.tecsup.lab13animaciones.fishing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.*

/** Original vector artwork: the same species silhouettes are used in water and in the collection. */
fun DrawScope.drawFish(fish: Fish, at: Offset, length: Float, facing: Float = 1f, time: Float = 0f, silhouette: Boolean = false) {
    val ink = if (silhouette) Color(0xFF263F4A) else Color(fish.color)
    val bodyHeight = when(fish.shape) { 1 -> .31f; 2 -> .65f; 3 -> .38f; else -> .5f }
    withTransform({ translate(at.x,at.y); scale(facing,1f,Offset.Zero) }) {
        val l = length; val h = l * bodyHeight * (1f + .035f*sin(time))
        if (fish.rarity == Rarity.LEGENDARY && !silhouette) {
            drawCircle(Brush.radialGradient(listOf(ink.copy(.4f),Color.Transparent),Offset.Zero,l),l)
        }
        val tail = Path().apply {
            moveTo(-l*.28f,0f); lineTo(-l*.62f, -h*(.6f+.12f*sin(time))); quadraticTo(-l*.52f,0f,-l*.62f,h*.62f); close()
        }
        drawPath(tail,ink.copy(alpha=.85f))
        val fin = Path().apply { moveTo(-l*.16f,-h*.25f); lineTo(-l*.04f,-h*.92f); lineTo(l*.25f,-h*.25f); close() }
        drawPath(fin,ink)
        drawOval(ink,Offset(-l*.37f,-h*.5f),Size(l*.82f,h))
        if (!silhouette) {
            drawOval(Color.White.copy(.24f),Offset(-l*.24f,-h*.35f),Size(l*.53f,h*.24f))
            if (fish.shape == 0) repeat(3) { i ->
                drawLine(Color(0xFFFDF3D2).copy(.8f),Offset(-l*.19f+i*l*.17f,-h*.38f),Offset(-l*.23f+i*l*.17f,h*.39f),l*.045f,StrokeCap.Round)
            }
            if (fish.shape == 3) repeat(4) { i -> drawCircle(Color.White.copy(.6f),l*.025f,Offset(-l*.22f+i*l*.12f,-h*.15f)) }
            val side = Path().apply { moveTo(0f,0f); lineTo(-l*.10f,h*.5f); lineTo(l*.16f,h*.18f); close() }
            drawPath(side,Color.Black.copy(.15f))
            drawCircle(Color(0xFFFFF8E9),l*.067f,Offset(l*.25f,-h*.08f))
            drawCircle(Color(0xFF162D3B),l*.032f,Offset(l*.27f,-h*.08f))
            drawArc(Color(0xFF203F4B).copy(.5f),90f,130f,false,Offset(l*.07f,-h*.27f),Size(l*.12f,h*.62f),style=Stroke(l*.016f))
            if (fish.shape == 1) drawLine(ink,Offset(l*.35f,0f),Offset(l*.58f,-h*.10f),l*.035f,StrokeCap.Round)
            if (fish.shape == 3) {
                drawLine(ink,Offset(l*.30f,h*.1f),Offset(l*.48f,h*.5f),l*.018f)
                drawLine(ink,Offset(l*.26f,h*.12f),Offset(l*.40f,h*.65f),l*.018f)
            }
            if (fish.rarity == Rarity.LEGENDARY) repeat(7) { i ->
                val angle = time+i*PI.toFloat()*2f/7f
                val r = l*(.58f+.05f*sin(time+i))
                drawCircle(Color(0xFFFFE9AF).copy(.5f+.3f*sin(time+i)),l*.018f,Offset(cos(angle)*r,sin(angle)*r*.6f))
            }
            when (fish.special) {
                FishSpecial.GOLD -> {
                    drawOval(Color(0xFFFFEF9A),Offset(-l*.37f,-h*.5f),Size(l*.82f,h),style=Stroke(l*.025f))
                    repeat(3) { i ->
                        val x=-l*.25f+i*l*.19f
                        val y=-h*(.65f+.16f*sin(time+i))
                        drawLine(Color(0xFFFFF5C1),Offset(x-l*.025f,y),Offset(x+l*.025f,y),l*.018f)
                        drawLine(Color(0xFFFFF5C1),Offset(x,y-l*.025f),Offset(x,y+l*.025f),l*.018f)
                    }
                }
                FishSpecial.CRYSTAL -> {
                    val facet=Path().apply {
                        moveTo(-l*.30f,0f);lineTo(-l*.08f,-h*.43f);lineTo(l*.17f,-h*.23f)
                        lineTo(l*.28f,h*.15f);lineTo(-l*.05f,h*.43f);close()
                    }
                    drawPath(facet,Brush.linearGradient(listOf(Color.White.copy(.8f),Color(0xFF74C8F6).copy(.25f))))
                    drawPath(facet,Color(0xFFE0FFFF),style=Stroke(l*.015f))
                    drawLine(Color.White.copy(.8f),Offset(-l*.08f,-h*.43f),Offset(-l*.05f,h*.43f),l*.012f)
                }
                FishSpecial.SECRET -> {
                    drawArc(Color(0xFFE1C5FF),210f,125f,false,Offset(-l*.24f,-h*.5f),Size(l*.36f,h*.8f),style=Stroke(l*.025f))
                    repeat(3) { i -> drawCircle(Color(0xFFC5B6FF),l*.022f,Offset(-l*.28f+i*l*.14f,h*.08f)) }
                }
                FishSpecial.LEGENDARY -> {
                    val crest=Path().apply {
                        moveTo(-l*.12f,-h*.4f);lineTo(-l*.15f,-h*.9f);lineTo(0f,-h*.65f)
                        lineTo(l*.10f,-h*1.1f);lineTo(l*.18f,-h*.42f);close()
                    }
                    drawPath(crest,Color(0xFFFFEAB0))
                }
                FishSpecial.NONE -> Unit
            }
            if (fish.id == "linterna") {
                drawLine(ink,Offset(l*.20f,-h*.3f),Offset(l*.33f,-h*.95f),l*.025f)
                drawCircle(Color(0xFFC8FFE2),l*.06f,Offset(l*.33f,-h*.95f))
            }
        }
    }
}

@Composable fun FishArt(fish: Fish, modifier: Modifier = Modifier, silhouette: Boolean = false) {
    val shimmer=rememberInfiniteTransition(label="Brillo de especie")
    val time by shimmer.animateFloat(0f,(PI*2).toFloat(),infiniteRepeatable(tween(4000,easing=LinearEasing)),label="Aletas y destellos")
    Canvas(modifier.clipToBounds().semantics { contentDescription = if(silhouette) "Especie sin descubrir" else fish.name }) {
        drawFish(fish,center,size.width*.7f,time=time,silhouette=silhouette)
    }
}

@Composable fun FishingScene(zone: Zone, rod: Rod, phase: FishingPhase, cast: Float, descent: Float,
                             reel: Float, catch: Catch?, modifier: Modifier = Modifier, targetDepth: Int = minOf(zone.max,rod.depth), showFish: Boolean = true, externalTime: Float? = null) {
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(externalTime == null) {
        if (externalTime != null) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            elapsed += ((now - previous) / 1_000_000_000f).coerceAtMost(.04f)
            previous = now
        }
    }
    val sceneTime = externalTime ?: elapsed
    val cycle = (sceneTime / 60f) % 1f
    val ambience by androidx.compose.animation.animateColorAsState(Color(zone.water),tween(900),label="Ambiente de profundidad")
    Canvas(modifier.clipToBounds().semantics { contentDescription = "Escena de ${zone.name}: pescador, barco y peces bajo el agua" }) {
        val w = size.width; val h = size.height; val water = h*.32f
        val seconds = sceneTime; val wave = sin(seconds*2f)*h*.004f
        drawRect(Brush.verticalGradient(listOf(Color(zone.sky),Color(0xFFF4EBCF)),endY=water))
        drawCircle(Color(0xFFFFE7A4),w*.065f,Offset(w*.80f,h*.09f))
        repeat(4) { i ->
            val x = ((i*.31f+cycle*1.35f)%1.35f-.15f)*w
            val y = h*(.055f+(i%2)*.045f)
            drawRoundRect(Color.White.copy(.65f),Offset(x,y),Size(w*.17f,h*.035f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(h*.03f))
            drawCircle(Color.White.copy(.65f),h*.023f,Offset(x+w*.07f,y))
        }
        fun mountain(x: Float, peak: Float, width: Float, color: Color) {
            drawPath(Path().apply { moveTo(x-width,water); lineTo(x,peak); lineTo(x+width,water);close() },color)
        }
        mountain(w*.18f,h*.08f,w*.38f,Color(0xFF8AADB0)); mountain(w*.75f,h*.13f,w*.4f,Color(0xFF6A9399))
        mountain(w*.1f,h*.22f,w*.4f,Color(0xFF467D70)); mountain(w*.93f,h*.19f,w*.38f,Color(0xFF537E6A))
        repeat(13) { i ->
            val x = w*(i/12f); val th = h*(.05f+(i%3)*.015f)
            drawLine(Color(0xFF5E6352),Offset(x,water),Offset(x,water-th),w*.007f)
            drawPath(Path().apply { moveTo(x-w*.025f,water-h*.01f);lineTo(x,water-th-h*.035f);lineTo(x+w*.025f,water-h*.01f);close() },Color(0xFF2D6258))
        }
        drawRect(Brush.verticalGradient(listOf(ambience.copy(.85f),ambience,Color(0xFF071C30)),water,h),Offset(0f,water),Size(w,h-water))
        repeat(4) { i ->
            val ray = Path().apply { moveTo(w*(.1f+i*.27f),water);lineTo(w*(.25f+i*.27f),water);lineTo(w*(.05f+i*.27f),h);lineTo(w*(-.15f+i*.27f),h);close() }
            drawPath(ray,Color(0xFFB2FFF1).copy(alpha=if(zone.id==3).025f else .065f))
        }
        // Seabed scrolls during the descent: foreground vegetation moves faster than fish.
        val camera = descent*h*.045f
        repeat(8) { i ->
            val x = w*i/7f
            drawOval(Color(0xFF344C52),Offset(x-w*.1f,h*.95f+camera),Size(w*.23f,h*.12f))
            repeat(3) { j ->
                val path=Path().apply { moveTo(x+j*w*.012f,h+camera);cubicTo(x-w*.05f,h*.9f,x+w*.05f*sin(seconds+j),h*.88f,x+w*.015f*sin(seconds+i),h*(.78f+j*.04f)+camera) }
                drawPath(path,if(zone.id==3)Color(0xFF6098AE) else Color(0xFF398F80),style=Stroke(w*.012f,cap=StrokeCap.Round))
            }
        }
        repeat(20) { i ->
            val x = w*((i*.137f+sin(seconds*.4f+i)*.013f)%1f)
            val y = water+((i*.053f+1f-seconds*(.012f+i%3*.004f))%1f+1f)%1f*(h-water)
            drawCircle(Color(0xFFCDFFF5).copy(.26f),w*(.003f+i%3*.002f),Offset(x,y),style=Stroke(w*.002f))
        }
        val minDepth=zone.min+1; val maxDepth=minOf(zone.max,rod.depth)
        val visible=species.filter { showFish && it.zone==zone.id && it.min<=maxDepth }
        visible.forEachIndexed { index, fish ->
            repeat(if(fish.rarity==Rarity.COMMON)3 else 1) { n ->
                val direction=if((index+n)%2==0)1f else -1f
                val speed= .016f*(70f/(fish.sizeCm+40f))
                val travel=((seconds*speed+(index*3+n)*.31f)%1f)*1.4f-.2f
                val x=if(direction==1f)travel*w else (1f-travel)*w
                val yDepth=(fish.min+minOf(fish.max,maxDepth))/2f
                val special = if(fish.rarity==Rarity.RARE || fish.rarity==Rarity.LEGENDARY) sin(seconds*1.3f+index)*h*.016f else 0f
                val y=water+h*.14f+(yDepth-minDepth)/(maxDepth-minDepth).coerceAtLeast(1)*h*.38f+sin(seconds*(.7f+n*.2f)+index)*h*.012f+camera+special
                drawFish(fish,Offset(x,y.coerceAtMost(h*.89f)),w*(.10f+(fish.sizeCm/400f).coerceAtMost(.20f)),direction,seconds*5f+n)
            }
        }
        repeat(8) { i ->
            val x=w*(i/7f); val path=Path().apply { moveTo(x-w*.05f,water+wave);quadraticTo(x,water-h*.009f+wave,x+w*.05f,water+wave) }
            drawPath(path,Color(0xFFBCF7EA).copy(.7f),style=Stroke(h*.004f))
        }
        val boatX=w*.29f; val boatY=water+wave-h*.01f
        withTransform({ translate(boatX,boatY) }) {
            drawOval(Color.Black.copy(.15f),Offset(-w*.15f,0f),Size(w*.32f,h*.025f))
            val hull=Path().apply { moveTo(-w*.16f,-h*.04f);lineTo(w*.19f,-h*.04f);quadraticTo(w*.13f,h*.055f,-w*.1f,h*.02f);close() }
            drawPath(hull,Color(0xFFAA5F41)); drawLine(Color(0xFFFFD79E),Offset(-w*.16f,-h*.04f),Offset(w*.19f,-h*.04f),h*.012f,StrokeCap.Round)
            drawLine(Color(0xFF664238),Offset(-w*.04f,-h*.042f),Offset(w*.02f,-h*.042f),h*.022f)
            // Fisherman boots, overalls, face and hat.
            drawLine(Color(0xFF253C4B),Offset(-w*.015f,-h*.06f),Offset(w*.035f,-h*.04f),h*.018f,StrokeCap.Round)
            drawLine(Color(0xFFDC9661),Offset(-w*.035f,-h*.105f),Offset(-w*.02f,-h*.05f),h*.038f,StrokeCap.Round)
            drawLine(Color(0xFF3E696F),Offset(-w*.035f,-h*.085f),Offset(-w*.02f,-h*.055f),h*.032f,StrokeCap.Round)
            drawCircle(Color(0xFFF1BA87),h*.023f,Offset(-w*.035f,-h*.14f))
            drawCircle(Color(0xFF192D3B),h*.0028f,Offset(-w*.014f,-h*.14f))
            drawOval(Color(0xFFE5B458),Offset(-w*.085f,-h*.172f),Size(w*.11f,h*.019f))
            drawRoundRect(Color(0xFFD39A47),Offset(-w*.061f,-h*.192f),Size(w*.055f,h*.033f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(5f))
        }
        val motion=if(phase==FishingPhase.CASTING)sin(cast*PI).toFloat() else 0f
        val grip=Offset(boatX+w*.05f,boatY-h*.095f)
        val tip=Offset(boatX+w*(.23f-motion*.24f),boatY-h*(.25f+motion*.04f))
        drawLine(Color(0xFFF1BA87),Offset(boatX-w*.03f,boatY-h*.105f),grip,h*.014f,StrokeCap.Round)
        drawLine(Color(rod.color),grip,tip,w*.007f,StrokeCap.Round)
        drawCircle(Color(0xFFDBE2D3),h*.011f,grip+Offset(0f,h*.012f),style=Stroke(h*.006f))
        if(phase!=FishingPhase.IDLE && phase!=FishingPhase.SUCCESS && phase!=FishingPhase.ESCAPED) {
            val end = when(phase) {
                FishingPhase.CASTING -> Offset(tip.x+(w*.75f-tip.x)*cast,tip.y+(water-tip.y)*cast-sin(cast*PI).toFloat()*h*.17f)
                else -> Offset(w*.75f,water+((h*.14f + ((targetDepth-minDepth).toFloat()/(maxDepth-minDepth).coerceAtLeast(1))*h*.38f)*descent)*(1f-reel))
            }
            drawLine(Color(0xFFECF9E7).copy(.85f),tip,end,w*.0025f)
            drawArc(Color(0xFFE1EFF0),0f,270f,false,end-Offset(w*.009f,0f),Size(w*.018f,h*.02f),style=Stroke(w*.004f))
            if(phase==FishingPhase.SINKING || phase==FishingPhase.BITE) {
                val radius = ((seconds*1.2f)%1f)*w*.075f
                drawOval(Color.White.copy(.35f),Offset(w*.75f-radius,water-radius*.2f),Size(radius*2,radius*.4f),style=Stroke(w*.003f))
            }
            if(catch!=null && (phase==FishingPhase.BITE || phase==FishingPhase.REELING || phase==FishingPhase.SINKING)) {
                val approach = if(phase==FishingPhase.SINKING)((descent-.55f)/.45f).coerceIn(0f,1f) else 1f
                drawFish(catch.fish,Offset(w*1.25f+(end.x+w*.035f-w*1.25f)*approach,end.y+h*.02f),w*.20f,-1f,seconds*8f)
            }
        }
    }
}
