package com.tecsup.lab13animaciones.fishing

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun FishingCollection(progress: Progress, back: () -> Unit) {
    var rarity by remember { mutableStateOf<Rarity?>(null) }
    var zone by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        PageHeader("Cuaderno del explorador",back)
        TitleBlock("Colección", "${progress.records.size} / ${species.size} especies", "${progress.total} capturas · Récord: ${progress.deepest} m")
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=rarity==null,onClick={rarity=null},label={Text("Todas")})
            Rarity.entries.forEach { r -> FilterChip(selected=rarity==r,onClick={rarity=r},label={Text(r.title)}) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=zone==null,onClick={zone=null},label={Text("Todas las zonas")})
            zones.forEach { z -> FilterChip(selected=zone==z.id,onClick={zone=z.id},label={Text(z.name)}) }
        }
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)) {
            items(species.filter { (rarity==null || it.rarity==rarity) && (zone==null || it.zone==zone) },key={it.id}) { fish ->
                val record=progress.records[fish.id]
                GameCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        FishArt(fish,Modifier.size(100.dp,80.dp),silhouette=record==null)
                        Column(Modifier.weight(1f)) {
                            Text(if(record==null)"Sin descubrir" else fish.name,color=Cream,style=MaterialTheme.typography.titleMedium)
                            Text(fish.rarity.title,color=rarityColor(fish.rarity))
                            if(fish.special!=FishSpecial.NONE) Text(fish.special.title,color=Gold,style=MaterialTheme.typography.bodySmall)
                            Text("${fish.behavior.title} · Nivel ${fish.requiredLevel}",color=Mint,style=MaterialTheme.typography.bodySmall)
                            Text("${zones[fish.zone].name} · ${fish.min}–${fish.max} m",color=Mint,style=MaterialTheme.typography.bodySmall)
                            Text(if(record==null)"Busca su silueta en el agua" else "${record.count} capturas · Mejor: ${record.bestCm} cm",color=Cream,style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable fun FishingZones(progress: Progress, selected: Int, choose: (Int) -> Unit, back: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        PageHeader("Atlas de aguas",back)
        TitleBlock("Explora", "Cuatro mundos por descubrir", "Tu equipo alcanza ${progress.rod.depth} metros")
        LazyColumn(verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(vertical=20.dp)) {
            items(zones,key={it.id}) { z ->
                val open=FishingLogic.available(z,progress)
                GameCard(Modifier.fillMaxWidth()) {
                    FishingScene(z,progress.rod,FishingPhase.IDLE,0f,0f,0f,null,Modifier.fillMaxWidth().height(140.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(z.name,color=Cream,style=MaterialTheme.typography.titleLarge)
                    Text("${z.min}–${z.max} m · ${species.count { it.zone==z.id }} especies",color=Mint)
                    GameButton(if(!open)"NIVEL ${Progression.zoneLevel(z.id)} · MÁS DE ${z.min} m" else if(selected==z.id)"ZONA ACTUAL · PESCAR" else "EXPLORAR",
                        Modifier.fillMaxWidth().padding(top=10.dp),enabled=open) { choose(z.id) }
                }
            }
        }
    }
}
