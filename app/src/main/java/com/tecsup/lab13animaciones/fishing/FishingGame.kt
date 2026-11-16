package com.tecsup.lab13animaciones.fishing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                                    if(ok) { progress=storage.load(); if(!FishingLogic.available(zones[zoneId],progress))zoneId=0 }
                                    else error="No se pudo guardar la compra. Inténtalo de nuevo."
                                    saving=false
                                }
                            }
                        }
                    },::home)
                    GamePage.COLLECTION -> FishingCollection(progress,::home)
                    GamePage.ZONES -> FishingZones(progress,zoneId,{zoneId=it;page=GamePage.WATER},::home)
                    GamePage.WATER -> FishingWater(progress,zones[zoneId],::home,{page=GamePage.COLLECTION},award={ catch ->
                        val ok=withContext(Dispatchers.IO) { storage.capture(catch) }
                        if(ok)progress=storage.load() else error="No se pudo guardar la captura. Usa Reintentar guardado."
                        ok
                    },recordDepth={ depth ->
                        val ok=withContext(Dispatchers.IO) { storage.explore(depth) }
                        if(ok) progress=storage.load()
                        ok
                    })
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
            LevelProgress(progress)
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
            MissionBoard(progress)
        }
    }
}
