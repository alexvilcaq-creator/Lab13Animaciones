package com.tecsup.lab13animaciones.fishing

import androidx.compose.ui.test.*
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
        if(label!="Volver al menu")node.performScrollTo()
        node.performClick()
        compose.mainClock.advanceTimeBy(600)
    }
    @Test fun laboratoryExercisesAndGameDestinationsRemainReachable() {
        compose.mainClock.autoAdvance=false
        listOf("Ejercicio 1: AnimatedVisibility","Ejercicio 2: animateColorAsState",
            "Ejercicio 3: Tamaño y Posicion","Ejercicio 4: AnimatedContent").forEach { label ->
            click(label)
            click("Volver al menu")
        }
        click("Ejercicio Final: Fishing Legends")
        compose.onNodeWithText("FISHING").assertIsDisplayed()
        click("TIENDA")
        compose.onNodeWithText("El taller de cañas").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performClick();compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithText("COLECCIÓN ·",substring=true).performScrollTo().performClick();compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithText("Cuaderno del explorador").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performClick();compose.mainClock.advanceTimeBy(600)
        click("ZONAS")
        compose.onNodeWithText("Atlas de aguas").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performClick();compose.mainClock.advanceTimeBy(600)
        click("JUGAR · LANZAR LA AVENTURA")
        compose.onNodeWithText("LANZAR CAÑA").assertIsDisplayed().performClick()
        compose.mainClock.advanceTimeBy(4100)
        compose.onNodeWithText("ATRAPAR").assertIsDisplayed()
        compose.onNodeWithText("‹ Puerto").performClick();compose.mainClock.advanceTimeBy(600)
        click("‹ Laboratorio")
        compose.onNodeWithText("Laboratorio 13: Animaciones").assertIsDisplayed()
    }
}
