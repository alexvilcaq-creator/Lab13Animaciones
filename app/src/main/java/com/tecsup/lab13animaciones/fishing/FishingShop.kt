package com.tecsup.lab13animaciones.fishing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun FishingShop(progress: Progress, onRod: (Int) -> Unit, back: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        PageHeader("El taller de cañas",back)
        TitleBlock("Equipo", "Más lejos, más profundo", "${progress.coins} monedas disponibles")
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=20.dp)) {
            items(rods,key={it.id}) { rod ->
                GameCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        RodArt(rod,Modifier.size(90.dp))
                        Column(Modifier.weight(1f)) {
                            Text(rod.name,color=Cream,style=MaterialTheme.typography.titleMedium)
                            Text("Hasta ${rod.depth} m · ayuda ${rod.id}/5",color=Mint,style=MaterialTheme.typography.bodySmall)
                            LinearProgressIndicator(progress={rod.depth/500f},modifier=Modifier.fillMaxWidth().padding(top=12.dp),color=Gold,trackColor=Night)
                        }
                    }
                    val owned=rod.id in progress.owned
                    val equipped=progress.equipped==rod.id
                    GameButton(when { equipped -> "EQUIPADA"; owned -> "EQUIPAR"; else -> "COMPRAR · ${rod.price} monedas" },
                        Modifier.fillMaxWidth(),enabled=!equipped && (owned || progress.coins>=rod.price),secondary=owned) { onRod(rod.id) }
                    if(!owned && progress.coins<rod.price) Text("Te faltan ${rod.price-progress.coins} monedas",color=Mint,style=MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
