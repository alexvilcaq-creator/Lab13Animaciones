package com.tecsup.lab13animaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Ejercicio1()
            }
        }
    }
}

@Composable
fun Ejercicio1() {

    var visible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Ejercicio 1: AnimatedVisibility",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                visible = !visible
            }
        ) {
            Text(
                text = if (visible) "Ocultar cuadro"
                else "Mostrar cuadro"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 700)
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 700)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        Color.Blue,
                        RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}
