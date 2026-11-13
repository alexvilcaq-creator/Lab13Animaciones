package com.tecsup.lab13animaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
                AppLaboratorio()
            }
        }
    }
}

// MENU PRINCIPAL
@Composable
fun AppLaboratorio() {

    var pantalla by remember {
        mutableStateOf(0)
    }

    when (pantalla) {
        0 -> MenuPrincipal(
            abrirEjercicio1 = { pantalla = 1 },
            abrirEjercicio2 = { pantalla = 2 }
        )

        1 -> Ejercicio1(
            volver = { pantalla = 0 }
        )

        2 -> Ejercicio2(
            volver = { pantalla = 0 }
        )
    }
}

@Composable
fun MenuPrincipal(
    abrirEjercicio1: () -> Unit,
    abrirEjercicio2: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Laboratorio 13: Animaciones",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = abrirEjercicio1) {
            Text("Ejercicio 1: Visibilidad")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = abrirEjercicio2) {
            Text("Ejercicio 2: Cambio de Color")
        }
    }
}

// EJERCICIO 1: ANIMATEDVISIBILITY
@Composable
fun Ejercicio1(volver: () -> Unit) {

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
                if (visible) "Ocultar cuadro"
                else "Mostrar cuadro"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(700)
            ),
            exit = fadeOut(
                animationSpec = tween(700)
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

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = volver) {
            Text("Volver al menu")
        }
    }
}

// EJERCICIO 2: ANIMATECOLORASSTATE
@Composable
fun Ejercicio2(volver: () -> Unit) {

    var cambiarColor by remember {
        mutableStateOf(false)
    }

    var usarSpring by remember {
        mutableStateOf(false)
    }

    val colorAnimado by animateColorAsState(
        targetValue = if (cambiarColor) {
            Color.Green
        } else {
            Color.Blue
        },
        animationSpec = if (usarSpring) {
            spring()
        } else {
            tween(durationMillis = 1000)
        },
        label = "AnimacionColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Ejercicio 2: Cambio de Color",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    colorAnimado,
                    RoundedCornerShape(12.dp)
                )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                cambiarColor = !cambiarColor
            }
        ) {
            Text(
                if (cambiarColor) "Cambiar a azul"
                else "Cambiar a verde"
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                usarSpring = !usarSpring
            }
        ) {
            Text(
                if (usarSpring) "Animacion: Spring"
                else "Animacion: Tween"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = volver) {
            Text("Volver al menu")
        }
    }
}

