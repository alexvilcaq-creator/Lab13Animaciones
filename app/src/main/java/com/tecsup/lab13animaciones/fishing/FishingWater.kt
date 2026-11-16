package com.tecsup.lab13animaciones.fishing

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable fun LevelProgress(progress: Progress) {
    val start=Progression.threshold(progress.level)
    val next=Progression.threshold(progress.level+1)
    Text("NIVEL ${progress.level} · ${progress.xp-start} / ${next-start} XP",color=Gold,lineHeight=22.sp)
    LinearProgressIndicator(progress={((progress.xp-start).toFloat()/(next-start).coerceAtLeast(1)).coerceIn(0f,1f)},
        modifier=Modifier.fillMaxWidth().height(8.dp),color=Gold,trackColor=Mint.copy(.2f))
}

/** Touch down starts a control, release/cancel always clears it, including pointer-input disposal. */
@Composable private fun HoldControl(label: String, enabled: Boolean, modifier: Modifier = Modifier, change: (Boolean) -> Unit) {
    val latestChange by rememberUpdatedState(change)
    Surface(color=if(enabled)Gold else Mint.copy(.18f),shape=MaterialTheme.shapes.large,
        modifier=modifier.heightIn(min=64.dp).testTag(label).semantics { role=Role.Button }
            .pointerInput(enabled) {
                if(enabled) detectTapGestures(onPress={
                    latestChange(true)
                    try { tryAwaitRelease() } finally { latestChange(false) }
                })
            }) {
        Box(Modifier.padding(horizontal=12.dp,vertical=18.dp),contentAlignment=Alignment.Center) {
            Text(label,color=if(enabled)Night else Mint,fontWeight=FontWeight.Black,lineHeight=24.sp)
        }
    }
}

@Composable fun FishingWater(progress: Progress, zone: Zone, back: () -> Unit, collection: () -> Unit,
                             award: suspend (Catch) -> Boolean) {
    val engine=remember(zone.id,progress.equipped) { FishingSimulation(zone,progress.rod) }
    var state by remember(engine) { mutableStateOf(engine.state) }
    var controls by remember { mutableStateOf(DiveControls()) }
    var confirmExit by remember { mutableStateOf(false) }
    var sessionOpen by rememberSaveable(zone.id,progress.equipped) { mutableStateOf(false) }
    var recreationNotice by remember { mutableStateOf(false) }
    LaunchedEffect(engine) {
        if(sessionOpen && engine.state.phase==DivePhase.PREPARADO) {
            recreationNotice=true;sessionOpen=false
        }
    }
    var savedToken by remember { mutableStateOf<String?>(null) }
    var saveFailed by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val lifecycle=(LocalActivity.current as? ComponentActivity)?.lifecycle
    var resumed by remember { mutableStateOf(lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED)==true) }
    val latestControls by rememberUpdatedState(controls)
    val latestAward by rememberUpdatedState(award)
    DisposableEffect(engine,lifecycle) {
        val observer=LifecycleEventObserver { _, _ ->
            resumed=lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED)==true
            if(!resumed) controls=DiveControls()
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer); controls=DiveControls(); engine.cancel() }
    }
    LaunchedEffect(engine,resumed,confirmExit) {
        if(!resumed || confirmExit) return@LaunchedEffect
        var previous=withFrameNanos { it }
        while(true) {
            val now=withFrameNanos { it }
            state=engine.advance((now-previous).coerceAtLeast(0)/1_000_000_000.0,latestControls)
            previous=now
        }
    }
    val caught=state.catch
    LaunchedEffect(caught?.token,retry) {
        if(caught==null || caught.token==savedToken) return@LaunchedEffect
        controls=DiveControls();saving=true;saveFailed=false
        try {
            if(latestAward(caught)) { savedToken=caught.token;sessionOpen=false } else saveFailed=true
        } finally { saving=false }
    }
    fun requestExit() {
        controls=DiveControls()
        if(saving) return
        if(state.active || (caught!=null && savedToken!=caught.token)) confirmExit=true else back()
    }
    BackHandler { requestExit() }
    if(confirmExit) AlertDialog(onDismissRequest={confirmExit=false},
        title={Text("¿Cancelar la sesión?")},text={Text("Volverás al puerto sin premios de esta sesión.")},
        confirmButton={TextButton(onClick={engine.cancel();state=engine.state;sessionOpen=false;confirmExit=false;back()}) { Text("CANCELAR Y SALIR") }},
        dismissButton={TextButton(onClick={confirmExit=false}) { Text("SEGUIR PESCANDO") }})
    val accent by animateColorAsState(if(state.tension>.8) androidx.compose.ui.graphics.Color(0xFFF38A7E) else Mint,label="Tensión del hilo")
    val noticePadding by animateDpAsState(if(state.phase==DivePhase.ENGANCHADO)12.dp else 6.dp,label="Aviso de enganche")
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={requestExit()}) { Text("‹ Puerto",color=Gold) }
            Text(zone.name,Modifier.weight(1f),color=Cream,fontSize=13.sp,lineHeight=19.sp)
            Text("Nv. ${progress.level} · ${progress.coins} M",color=Gold,fontSize=12.sp)
        }
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min=120.dp)) {
            DiveScene(zone,progress.rod,state,Modifier.fillMaxSize())
            Text("${"%.1f".format(state.depth)} m / ${engine.maxDepth.toInt()} m",
                Modifier.align(Alignment.TopEnd).testTag("dive-status").semantics { stateDescription="x=${state.x};depth=${state.depth}" }.padding(8.dp).background(Night.copy(.85f),MaterialTheme.shapes.medium).padding(8.dp),color=Cream)
            androidx.compose.animation.AnimatedVisibility(visible=state.phase==DivePhase.ENGANCHADO,modifier=Modifier.align(Alignment.TopCenter)) {
                Text("¡PICADA!",Modifier.padding(top=46.dp).background(Gold,MaterialTheme.shapes.medium).padding(noticePadding),
                    color=Night,fontWeight=FontWeight.Black)
            }
        }
        Column(Modifier.fillMaxWidth().heightIn(max=300.dp).verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            AnimatedContent(state.phase,label="Fases de exploración") { phase ->
                Text(when(phase) {
                    DivePhase.PREPARADO -> "PREPARADO · ${progress.rod.name}"
                    DivePhase.LANZANDO -> "LANZANDO · Entrada al agua"
                    DivePhase.DESCENDIENDO -> if(state.depth>=engine.maxDepth-.01)"Límite de la caña · Explora a los lados" else "DESCENDIENDO · Busca contacto con un pez"
                    DivePhase.ENGANCHADO -> "ENGANCHADO · Suelta para aliviar la tensión"
                    DivePhase.RECOGIENDO -> "RECOGIENDO · Evita mantener el 100 %"
                    DivePhase.RESULTADO -> state.message
                },color=accent,fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=21.sp)
            }
            if(state.phase==DivePhase.PREPARADO) {
                if(recreationNotice) Text("Sesión cerrada al recrear la pantalla. Las capturas ya guardadas se conservan.",color=Mint,fontSize=12.sp,lineHeight=18.sp)
                LevelProgress(progress)
                GameButton("LANZAR CAÑA",Modifier.fillMaxWidth(),enabled=resumed && FishingLogic.available(zone,progress)) {
                    engine.launch();state=engine.state;sessionOpen=true;recreationNotice=false;savedToken=null;saveFailed=false
                }
                Text("Mantén los controles para dirigir el anzuelo. La picada ocurre al tocar un pez.",color=Mint,fontSize=12.sp,lineHeight=18.sp)
            }
            if(state.active) {
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    HoldControl("IZQUIERDA",resumed && !confirmExit && state.phase!=DivePhase.LANZANDO,Modifier.weight(1f)) { controls=controls.copy(left=it) }
                    HoldControl("DERECHA",resumed && !confirmExit && state.phase!=DivePhase.LANZANDO,Modifier.weight(1f)) { controls=controls.copy(right=it) }
                }
            }
            if(state.hooked!=null && state.phase!=DivePhase.RESULTADO) {
                Text("${state.hooked!!.fish.name} · TENSIÓN ${(state.tension*100).toInt()} %",color=accent,lineHeight=22.sp)
                LinearProgressIndicator(progress={state.tension.toFloat()},modifier=Modifier.fillMaxWidth().height(14.dp),color=accent,trackColor=Mint.copy(.2f))
                HoldControl("RECOGER",resumed && !confirmExit,Modifier.fillMaxWidth()) { controls=controls.copy(reel=it) }
            }
            if(state.phase==DivePhase.RESULTADO) {
                caught?.let {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        FishArt(it.fish,Modifier.size(80.dp,56.dp))
                        Text("${it.fish.name} · ${it.sizeCm} cm\n+${it.fish.value} monedas · +${Progression.catchXp(it.fish.rarity)} XP",
                            color=Gold,fontSize=14.sp,lineHeight=22.sp)
                    }
                    Text(if(savedToken==it.token)"Captura guardada" else if(saveFailed)"No se pudo guardar. Reintenta antes de salir." else "Guardando captura…",color=Mint)
                    if(saveFailed) GameButton("REINTENTAR GUARDADO",Modifier.fillMaxWidth(),enabled=!saving) { retry++ }
                }
                GameButton("PESCAR DE NUEVO",Modifier.fillMaxWidth(),enabled=caught==null || savedToken==caught.token) {
                    engine.cancel();state=engine.state;sessionOpen=false;controls=DiveControls()
                }
                if(caught!=null && savedToken==caught.token) TextButton(onClick=collection) { Text("VER COLECCIÓN",color=Gold) }
            }
        }
    }
}
