package com.tecsup.lab13animaciones.fishing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun MissionBoard(progress: Progress) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("MISIONES DE EXPLORACIÓN",color=Gold,fontSize=13.sp,lineHeight=20.sp)
        FishingMissions.all.forEach { mission ->
            val done=mission.id in progress.completedMissions
            val count=mission.metric(progress).coerceAtMost(mission.target)
            Text("${mission.title} · $count/${mission.target}",color=Cream,fontSize=13.sp,lineHeight=20.sp)
            LinearProgressIndicator(progress={count.toFloat()/mission.target},modifier=Modifier.fillMaxWidth(),color=Mint,trackColor=Mint.copy(.15f))
            Text(if(done)"Completada · premio entregado" else "+${mission.coins} monedas · +${mission.xp} XP automáticos",color=Mint,fontSize=12.sp,lineHeight=18.sp)
        }
    }
}
