package com.tecsup.lab13animaciones.fishing

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable fun FishingGame(onExit: () -> Unit) {
    val context=LocalContext.current
    val storage=remember { FishingStorage(context) }
    var progress by remember { mutableStateOf(storage.load()) }
    var page by rememberSaveable { mutableStateOf(GamePage.HOME) }
    var zoneId by rememberSaveable { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope=rememberCoroutineScope()
    fun home() { page=GamePage.HOME }
    BackHandler { if(page==GamePage.HOME)onExit() else home() }
    MaterialTheme(colorScheme=darkColorScheme(primary=Gold,onPrimary=Night,background=Night,surface=Night,onSurface=Cream)) {
        Column(Modifier.fillMaxSize().background(Night).safeDrawingPadding()) {
            AnimatedVisibility(error!=null) {
                Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(error.orEmpty(),Modifier.weight(1f),color=Gold)
                    TextButton(onClick={error=null}) { Text("Cerrar") }
                }
            }
            AnimatedContent(targetState=page,modifier=Modifier.weight(1f),transitionSpec={
                (fadeIn(tween(250))+slideInHorizontally { it/12 }) togetherWith fadeOut(tween(180))
            },label="Pantallas Fishing Legends") { target ->
                when(target) {
                    GamePage.HOME -> FishingHome(progress,zones[zoneId],{page=it},onExit)
                    GamePage.SHOP -> FishingShop(progress,{ id ->
                        if(!saving) {
                            val next=if(id in progress.owned)FishingLogic.equip(progress,id) else FishingLogic.buy(progress,id)
                            if(next!=progress) {
                                saving=true
                                scope.launch {
                                    val ok=withContext(Dispatchers.IO) { storage.changeRod(id) }
                                    if(ok) { progress=storage.load(); if(!FishingLogic.available(zones[zoneId],next.rod))zoneId=0 }
                                    else error="No se pudo guardar la compra. Inténtalo de nuevo."
                                    saving=false
                                }
                            }
                        }
                    },::home)
                    GamePage.COLLECTION -> FishingCollection(progress,::home)
                    GamePage.ZONES -> FishingZones(progress,zoneId,{zoneId=it;page=GamePage.WATER},::home)
                    GamePage.WATER -> FishingWater(progress,zones[zoneId],::home,{page=GamePage.COLLECTION}) { catch ->
                        val ok=withContext(Dispatchers.IO) { storage.capture(catch) }
                        if(ok)progress=storage.load() else error="No se pudo guardar la captura. Usa Reintentar guardado."
                        ok
                    }
                }
            }
        }
    }
}

@Composable private fun FishingHome(progress: Progress, zone: Zone, navigate: (GamePage) -> Unit, exit: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick=exit) { Text("‹ Laboratorio",color=Mint) }
            Spacer(Modifier.weight(1f)); Text("${progress.coins} MONEDAS",color=Gold,fontWeight=FontWeight.Bold)
        }
        Column(Modifier.padding(horizontal=24.dp)) {
            Text("UN LANCE. MIL HISTORIAS.",color=Mint,fontSize=10.sp,letterSpacing=3.sp)
            Text("FISHING",color=Cream,fontSize=42.sp,lineHeight=50.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp)
            Text("LEGENDS",color=Gold,fontSize=42.sp,lineHeight=50.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp)
        }
        FishingScene(zone,progress.rod,FishingPhase.IDLE,0f,0f,0f,null,Modifier.fillMaxWidth().height(290.dp))
        Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("${zone.name} · ${progress.rod.name}",color=Mint,fontSize=13.sp)
            GameButton("JUGAR · LANZAR LA AVENTURA",Modifier.fillMaxWidth()) { navigate(GamePage.WATER) }
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                GameButton("TIENDA",Modifier.weight(1f),secondary=true) { navigate(GamePage.SHOP) }
                GameButton("ZONAS",Modifier.weight(1f),secondary=true) { navigate(GamePage.ZONES) }
            }
            GameButton("COLECCIÓN · ${progress.records.size}/${species.size}",Modifier.fillMaxWidth(),secondary=true) { navigate(GamePage.COLLECTION) }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Stat("CAPTURAS",progress.total.toString()); Stat("RÉCORD", "${progress.deepest} m"); Stat("ALCANCE","${progress.rod.depth} m")
            }
        }
    }
}

@Composable private fun FishingWater(progress: Progress, zone: Zone, back: () -> Unit, collection: () -> Unit, award: suspend (Catch) -> Boolean) {
    var phase by remember { mutableStateOf(FishingPhase.IDLE) }
    var depth by remember { mutableFloatStateOf(minOf(zone.max,progress.rod.depth).toFloat()) }
    var hooked by remember { mutableStateOf<Catch?>(null) }
    val cast=remember { Animatable(0f) }; val descent=remember { Animatable(0f) }; val reel=remember { Animatable(0f) }
    var needle by remember { mutableFloatStateOf(0f) }
    var center by remember { mutableFloatStateOf(.5f) }
    var saveFailed by remember { mutableStateOf(false) }
    var retrying by remember { mutableStateOf(false) }
    val scope=rememberCoroutineScope()
    val lifecycle=(LocalActivity.current as? ComponentActivity)?.lifecycle
    var resumed by remember { mutableStateOf(lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) }
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver { _, _ -> resumed=lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }
    val latestAward by rememberUpdatedState(award)
    LaunchedEffect(phase,resumed) {
        if(!resumed)return@LaunchedEffect
        when(phase) {
            FishingPhase.CASTING -> { cast.animateTo(1f,tween(1000)); phase=FishingPhase.SINKING }
            FishingPhase.SINKING -> { descent.animateTo(1f,tween(2200,easing=LinearEasing)); delay(650);phase=FishingPhase.BITE }
            FishingPhase.BITE -> {
                var elapsed=0f; var previous=withFrameNanos { it }
                val fish=hooked?.fish ?: return@LaunchedEffect
                val duration=9000f-fish.difficulty*2500f
                while(elapsed<duration) {
                    val now=withFrameNanos { it }; elapsed+=((now-previous)/1_000_000f).coerceAtMost(40f); previous=now
                    needle=(sin(elapsed/1000f*(2.3f+fish.difficulty*3f)-PI/2).toFloat()+1f)/2f
                }
                phase=FishingPhase.ESCAPED
            }
            FishingPhase.REELING -> {
                reel.animateTo(1f,tween(1800))
                val catch=hooked ?: return@LaunchedEffect
                if(latestAward(catch))phase=FishingPhase.SUCCESS else saveFailed=true
            }
            else -> Unit
        }
    }
    val accent by animateColorAsState(when(phase) {
        FishingPhase.BITE -> Gold; FishingPhase.SUCCESS -> Mint; FishingPhase.ESCAPED -> androidx.compose.ui.graphics.Color(0xFFF38A7E); else -> Mint
    },label="Estado de pesca")
    val statusSize by animateDpAsState(if(phase==FishingPhase.BITE)12.dp else 8.dp,label="Aviso de picada")
    fun reset() { scope.launch { cast.snapTo(0f);descent.snapTo(0f);reel.snapTo(0f);saveFailed=false;phase=FishingPhase.IDLE } }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick=back) { Text("‹ Puerto",color=Gold) }
            Text(zone.name,Modifier.weight(1f),color=Cream,fontSize=13.sp)
            Text("${progress.coins} M",color=Gold,fontWeight=FontWeight.Bold);Spacer(Modifier.width(12.dp))
        }
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min=160.dp)) {
            FishingScene(zone,progress.rod,phase,cast.value,descent.value,reel.value,hooked,Modifier.fillMaxSize(),targetDepth=depth.toInt())
            Text("${(depth*descent.value*(1f-reel.value)).toInt()} m / ${progress.rod.depth} m",
                Modifier.align(Alignment.TopEnd).padding(12.dp).background(Night.copy(.8f),MaterialTheme.shapes.medium).padding(8.dp),color=Cream,fontSize=12.sp)
            androidx.compose.animation.AnimatedVisibility(visible=phase==FishingPhase.BITE,modifier=Modifier.align(Alignment.Center)) {
                Text("¡PICADA!",Modifier.background(Gold,MaterialTheme.shapes.large).padding(horizontal=24.dp,vertical=statusSize),color=Night,fontWeight=FontWeight.Black,fontSize=24.sp,lineHeight=32.sp)
            }
        }
        Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            AnimatedContent(phase,label="Estados de la captura") { current ->
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    when(current) {
                        FishingPhase.IDLE -> {
                            Text("ELIGE TU PROFUNDIDAD",color=accent,fontSize=11.sp,letterSpacing=2.sp)
                            Text("${depth.toInt()} metros · ${progress.rod.name}",color=Cream)
                            Slider(value=depth,onValueChange={if(phase==FishingPhase.IDLE)depth=it},valueRange=(zone.min+1).toFloat()..minOf(zone.max,progress.rod.depth).toFloat())
                            GameButton("LANZAR CAÑA",Modifier.fillMaxWidth()) {
                                if(phase==FishingPhase.IDLE) {
                                    val fish=FishingLogic.select(zone,progress.rod,depth.toInt())
                                    if(fish!=null) {
                                        hooked=Catch(UUID.randomUUID().toString(),fish,depth.toInt(),(fish.sizeCm*Random.nextDouble(.8,1.3)).toInt())
                                        center=Random.nextDouble(.3,.7).toFloat();phase=FishingPhase.CASTING
                                    }
                                }
                            }
                            Text("Pulsa ATRAPAR cuando la marca entre en la franja dorada.",color=Mint,fontSize=12.sp)
                        }
                        FishingPhase.BITE -> {
                            Text("¡Ha mordido! Pulsa en el momento justo.",color=accent)
                            val width=FishingLogic.targetWidth(hooked!!.fish,progress.rod)
                            CaptureBar(needle,center,width)
                            GameButton("ATRAPAR",Modifier.fillMaxWidth()) {
                                if(phase==FishingPhase.BITE)phase=if(FishingLogic.hit(needle,center,width))FishingPhase.REELING else FishingPhase.ESCAPED
                            }
                        }
                        FishingPhase.SUCCESS -> hooked?.let { fish ->
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                FishArt(fish.fish,Modifier.size(120.dp,85.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(fish.fish.name,color=Cream,fontWeight=FontWeight.Bold,fontSize=21.sp,lineHeight=28.sp)
                                    Text("${fish.fish.rarity.title} · ${fish.sizeCm} cm",color=rarityColor(fish.fish.rarity))
                                    Text("${fish.depth} m · +${fish.fish.value} monedas",color=Gold)
                                }
                            }
                            AnimatedVisibility(visible=true,enter=scaleIn()+fadeIn()) { Text("Captura registrada en tu cuaderno",color=Mint,fontSize=12.sp) }
                            GameButton("PESCAR DE NUEVO",Modifier.fillMaxWidth()) { reset() }
                            TextButton(onClick=collection) { Text("VER COLECCIÓN",color=Gold) }
                        }
                        FishingPhase.ESCAPED -> {
                            Text("Se escapó…",color=accent,fontWeight=FontWeight.Bold,fontSize=24.sp,lineHeight=32.sp)
                            Text("Espera a que la marca entre en la franja dorada. ¡Otro lance!",color=Mint)
                            GameButton("VOLVER A LANZAR",Modifier.fillMaxWidth()) { reset() }
                        }
                        else -> {
                            Text(when(current) { FishingPhase.CASTING -> "Preparando el lance…"; FishingPhase.SINKING -> "El anzuelo desciende…"; else -> "Recogiendo el hilo…" },color=accent,fontWeight=FontWeight.Bold)
                            Text(if(saveFailed)"La captura está pendiente de guardar." else "Observa las especies bajo la superficie.",color=Mint,fontSize=12.sp)
                            if(saveFailed)GameButton("REINTENTAR GUARDADO",enabled=!retrying) {
                                if(!retrying) { retrying=true;scope.launch { if(latestAward(hooked!!)){saveFailed=false;phase=FishingPhase.SUCCESS};retrying=false } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun CaptureBar(position: Float, center: Float, width: Float) {
    val color by animateColorAsState(if(FishingLogic.hit(position,center,width))Mint else Gold,label="Precisión del minijuego")
    Canvas(Modifier.fillMaxWidth().height(46.dp)) {
        val inset=8.dp.toPx();val w=size.width-2*inset
        drawRoundRect(androidx.compose.ui.graphics.Color(0xFF315561),size=size,cornerRadius=androidx.compose.ui.geometry.CornerRadius(inset))
        drawRect(Gold.copy(.65f),Offset(inset+(center-width/2)*w,4.dp.toPx()),Size(width*w,size.height-8.dp.toPx()))
        drawLine(color,Offset(inset+position*w,0f),Offset(inset+position*w,size.height),5.dp.toPx())
    }
}
