package com.tecsup.lab13animaciones.fishing

import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.lab13animaciones.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FishingNavigationTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun click(label: String) {
        val node=compose.onNodeWithText(label)
        if(label.startsWith("Ejercicio")) {
            compose.onNodeWithTag("lab-menu").performScrollToNode(hasText(label))
            node.assertHeightIsAtLeast(88.dp)
        } else node.performScrollTo()
        node.assertIsDisplayed().assertHasClickAction().performTouchInput { click(center) }
        compose.mainClock.advanceTimeBy(600)
    }
    @Test fun laboratoryExercisesAndGameDestinationsRemainReachable() {
        compose.mainClock.autoAdvance=false
        listOf("Ejercicio 1: AnimatedVisibility","Ejercicio 2: animateColorAsState",
            "Ejercicio 3: Tamaño y Posicion","Ejercicio 4: AnimatedContent").forEach { label ->
            click(label)
            when {
                label.startsWith("Ejercicio 1") -> {
                    click("Mostrar cuadro")
                    compose.onNodeWithText("Ocultar cuadro").assertIsDisplayed()
                }
                label.startsWith("Ejercicio 2") -> {
                    click("Cambiar a verde")
                    compose.onNodeWithText("Cambiar a azul").assertIsDisplayed()
                }
                label.startsWith("Ejercicio 3") -> {
                    click("Mover y agrandar")
                    compose.onNodeWithText("Regresar cuadro").assertIsDisplayed()
                }
                else -> {
                    click("Contenido")
                    compose.onNodeWithText("Contenido cargado correctamente").assertIsDisplayed()
                }
            }
            click("Volver al menu")
        }
        click("Ejercicio Final: Fishing Legends")
        compose.onNodeWithText("FISHING").assertIsDisplayed()
        click("TIENDA")
        compose.onNodeWithText("El taller de cañas").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performTouchInput { click(center) };compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithText("COLECCIÓN ·",substring=true).performScrollTo().performTouchInput { click(center) };compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithText("Cuaderno del explorador").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performTouchInput { click(center) };compose.mainClock.advanceTimeBy(600)
        click("ZONAS")
        compose.onNodeWithText("Atlas de aguas").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performTouchInput { click(center) };compose.mainClock.advanceTimeBy(600)
        click("JUGAR · LANZAR LA AVENTURA")
        compose.onNodeWithText("LANZAR CAÑA").assertIsDisplayed().performTouchInput { click(center) }
        compose.mainClock.advanceTimeBy(4100)
        compose.onNodeWithText("ATRAPAR").assertIsDisplayed()
        compose.onNodeWithText("‹ Puerto").performTouchInput { click(center) };compose.mainClock.advanceTimeBy(600)
        click("‹ Laboratorio")
        compose.onNodeWithText("Laboratorio 13: Animaciones").assertIsDisplayed()
    }
}
