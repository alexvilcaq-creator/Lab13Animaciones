
package com.tecsup.lab13animaciones

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.tecsup.lab13animaciones.fishing.FishingGame
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.testTag
import com.tecsup.lab13animaciones.ui.theme.Lab13AnimacionesTheme
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Lab13AnimacionesTheme(darkTheme = false, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppLaboratorio()
                }
            }
        }
    }
}

// MENU PRINCIPAL
@Composable
fun AppLaboratorio() {
    var pantalla by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = pantalla in 1..4) { pantalla = 0 }
    when (pantalla) {
        0 -> MenuPrincipal(
            abrirEjercicio1 = { pantalla = 1 },
            abrirEjercicio2 = { pantalla = 2 },
            abrirEjercicio3 = { pantalla = 3 },
            abrirEjercicio4 = { pantalla = 4 },
            abrirFishing = { pantalla = 5 }
        )

        1 -> Ejercicio1(volver = { pantalla = 0 })
        2 -> Ejercicio2(volver = { pantalla = 0 })
        3 -> Ejercicio3(volver = { pantalla = 0 })
        4 -> Ejercicio4(volver = { pantalla = 0 })
        5 -> FishingGame(onExit = { pantalla = 0 })
    }
}

// MENU DE EJERCICIOS
@Composable
fun MenuPrincipal(
    abrirEjercicio1: () -> Unit,
    abrirEjercicio2: () -> Unit,
    abrirEjercicio3: () -> Unit,
    abrirEjercicio4: () -> Unit,
    abrirFishing: () -> Unit
) {
    val entries = listOf(
        Triple("Ejercicio 1: AnimatedVisibility", "Mostrar y ocultar elementos", abrirEjercicio1),
        Triple("Ejercicio 2: animateColorAsState", "Explorar transiciones de color", abrirEjercicio2),
        Triple("Ejercicio 3: Tamaño y Posicion", "Animar dimensiones y desplazamiento", abrirEjercicio3),
        Triple("Ejercicio 4: AnimatedContent", "Cambiar contenido con transiciones", abrirEjercicio4),
        Triple("Ejercicio Final: Fishing Legends", "Jugar, explorar y coleccionar peces", abrirFishing)
    )
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(vertical = 20.dp)) {
            Text("LABORATORIO 13", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text("Laboratorio 13: Animaciones", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp))
            Text("Selecciona un ejercicio para comenzar.", style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp))
        }
        LazyColumn(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().weight(1f).testTag("lab-menu"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries.indices.toList(), key = { it }) { index ->
                val (title, description, open) = entries[index]
                Button(
                    onClick = open,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
                        .testTag("lab-destination-${index + 1}"),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (index == 4) Color(0xFF176B63) else Color(0xFF354A78)
                    )
                ) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium,
                            fontSize = 17.sp, lineHeight = 24.sp)
                        Text(description, style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

// EJERCICIO 1: VISIBILIDAD
@Composable
fun Ejercicio1(volver: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ejercicio 1: AnimatedVisibility",
            fontSize = 22.sp,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = { visible = !visible }) {
            Text(
                if (visible) "Ocultar cuadro"
                else "Mostrar cuadro"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(700)),
            exit = fadeOut(animationSpec = tween(700))
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

        Button(onClick = volver, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Volver al menu")
        }
    }
}

// EJERCICIO 2: CAMBIO DE COLOR
@Composable
fun Ejercicio2(volver: () -> Unit) {
    var cambiarColor by remember { mutableStateOf(false) }
    var usarSpring by remember { mutableStateOf(false) }

    val colorAnimado by animateColorAsState(
        targetValue = if (cambiarColor) Color.Green else Color.Blue,
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
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ejercicio 2: Cambio de Color",
            fontSize = 22.sp,
            lineHeight = 30.sp
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

        Button(onClick = { cambiarColor = !cambiarColor }) {
            Text(
                if (cambiarColor) "Cambiar a azul"
                else "Cambiar a verde"
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = { usarSpring = !usarSpring }) {
            Text(
                if (usarSpring) "Animacion: Spring"
                else "Animacion: Tween"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = volver, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Volver al menu")
        }
    }
}

// EJERCICIO 3: TAMAÑO Y POSICION
@Composable
fun Ejercicio3(volver: () -> Unit) {
    var mover by remember { mutableStateOf(false) }

    val tamaño by animateDpAsState(
        targetValue = if (mover) 180.dp else 100.dp,
        animationSpec = tween(durationMillis = 800),
        label = "AnimacionTamaño"
    )

    val posicionX by animateDpAsState(
        targetValue = if (mover) 80.dp else (-80).dp,
        animationSpec = tween(durationMillis = 800),
        label = "AnimacionPosicion"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ejercicio 3: Tamaño y Posicion",
            fontSize = 22.sp,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = posicionX)
                    .size(tamaño)
                    .background(
                        Color(0xFF2196F3),
                        RoundedCornerShape(12.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { mover = !mover }) {
            Text(
                if (mover) "Regresar cuadro"
                else "Mover y agrandar"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = volver, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Volver al menu")
        }
    }
}

// EJERCICIO 4: ANIMATEDCONTENT
@Composable
fun Ejercicio4(volver: () -> Unit) {
    var estado by remember { mutableStateOf("Cargando") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ejercicio 4: AnimatedContent",
            fontSize = 22.sp,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedContent(
            targetState = estado,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(900)
                ) togetherWith fadeOut(
                    animationSpec = tween(400)
                )
            },
            label = "CambioContenido"
        ) { estadoActual ->

            val color = when (estadoActual) {
                "Cargando" -> Color(0xFF2196F3)
                "Contenido" -> Color(0xFF4CAF50)
                else -> Color(0xFFF44336)
            }

            val mensaje = when (estadoActual) {
                "Cargando" -> "Cargando informacion..."
                "Contenido" -> "Contenido cargado correctamente"
                else -> "Error al cargar la informacion"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mensaje,
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { estado = "Cargando" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cargando")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { estado = "Contenido" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Contenido")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { estado = "Error" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Error")
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(onClick = volver, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Volver al menu")
        }
    }
}
