package com.tecsup.lab13animaciones.fishing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Night = Color(0xFF142F3B)
val Cream = Color(0xFFF7EED9)
val Gold = Color(0xFFF4BD59)
val Mint = Color(0xFF89CBB1)
fun rarityColor(rarity: Rarity) = when(rarity) {
    Rarity.COMMON -> Mint; Rarity.UNCOMMON -> Color(0xFF84C3E9)
    Rarity.RARE -> Color(0xFFD1ADFA); Rarity.LEGENDARY -> Gold
}
@Composable fun GameButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, secondary: Boolean = false, onClick: () -> Unit) {
    Button(onClick,modifier.heightIn(min=50.dp),enabled=enabled,shape=RoundedCornerShape(16.dp),
        colors=ButtonDefaults.buttonColors(containerColor=if(secondary)Color(0xFF315561) else Gold,
            contentColor=if(secondary)Cream else Night)) {
        Text(text,fontWeight=FontWeight.Bold,letterSpacing=.5.sp)
    }
}
@Composable fun GameCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(Color(0xFF203F4B),RoundedCornerShape(22.dp)).padding(16.dp),content=content)
}
@Composable fun TitleBlock(eyebrow: String, title: String, subtitle: String) {
    Text(eyebrow.uppercase(),color=Gold,fontSize=11.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold)
    Text(title,color=Cream,fontSize=28.sp,fontWeight=FontWeight.Black)
    Text(subtitle,color=Mint,fontSize=13.sp)
}
@Composable fun RodArt(rod: Rod, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val start=Offset(size.width*.18f,size.height*.82f); val end=Offset(size.width*.85f,size.height*.12f)
        drawLine(Color(rod.color),start,end,5.dp.toPx(),StrokeCap.Round)
        drawLine(Color(0xFF70503C),start,Offset(size.width*.34f,size.height*.65f),8.dp.toPx(),StrokeCap.Round)
        drawCircle(Color(rod.color),10.dp.toPx(),Offset(size.width*.33f,size.height*.75f))
        drawCircle(Night,5.dp.toPx(),Offset(size.width*.33f,size.height*.75f))
        drawLine(Cream.copy(.7f),end,Offset(size.width*.85f,size.height*.85f),1.dp.toPx())
        repeat(rod.id+1) { i -> drawCircle(Gold,2.dp.toPx(),Offset(size.width*(.42f+i*.065f),size.height*(.58f-i*.065f))) }
    }
}
@Composable fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) { Text(value,color=Cream,fontWeight=FontWeight.Bold,fontSize=20.sp); Text(label,color=Mint,fontSize=11.sp) }
}
@Composable fun PageHeader(title: String, back: () -> Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        TextButton(onClick=back) { Text("‹ Volver",color=Gold) }
        Text(title,color=Cream,fontWeight=FontWeight.Bold,fontSize=19.sp)
    }
}
