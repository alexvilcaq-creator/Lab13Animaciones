# Fishing Legends

Juego Android offline en Kotlin y Jetpack Compose, accesible desde **Ejercicio Final: Fishing Legends**. Los ejercicios 1–4 siguen en el menú. No requiere permisos de Internet, cuentas, servicios ni nuevos paquetes.

## Jugar

1. Entra en JUGAR. Selecciona la profundidad con el deslizador y pulsa LANZAR CAÑA.
2. Observa el lance, descenso y aproximación del pez. Al aparecer ¡PICADA!, pulsa ATRAPAR cuando la marca cruce la franja dorada. Hay aproximadamente 7–9 segundos antes de que escape.
3. Un acierto inicia la recogida y registra monedas, tamaño, profundidad y especie. Una pulsación fallida pierde el pez, sin coste. PESCAR DE NUEVO inicia otro lance.
4. Compra/equipa cañas en TIENDA. ZONAS bloquea aguas que exceden el alcance del equipo. Todas las profundidades accesibles tienen al menos una especie.
5. COLECCIÓN muestra las especies descubiertas, siluetas pendientes, número de capturas y mejor tamaño, con filtros de rareza y zona.

Se empieza con cero monedas y la caña de madera. Las recompensas permiten comprar progresivamente las seis cañas. Las doce especies tienen IDs estables; se pueden añadir más al catálogo sin modificar la colección ni invalidar las partidas.

## Organización

- `fishing/FishingModels.kt`: catálogos, rarezas y modelos inmutables.
- `fishing/FishingLogic.kt`: compras, equipo, límites, selección ponderada, precisión y recompensas idempotentes.
- `fishing/FishingStorage.kt`: progreso en SharedPreferences privadas, un documento JSON con escritura atómica y confirmación de guardado en IO. Una captura lleva un identificador para impedir doble pago al reanudar/reintentar.
- `fishing/FishingGame.kt`: navegación, portada, estados de pesca y minijuego. Al abandonar una pesca incompleta se cancela; lo ya registrado permanece. Al volver del segundo plano se reanuda el estado visual y la ventana de captura se reinicia, sin penalización.
- `fishing/FishingScene.kt`: ilustraciones vectoriales originales, paisajes, agua, vegetación, peces y pescador; no usa emojis ni imágenes remotas. El lago es una vista en corte; el descenso desplaza el entorno y cambia el punto del anzuelo. No es una cámara 3D.
- `fishing/FishingComponents.kt`, `FishingShop.kt`, `FishingCollection.kt`: controles, tienda, atlas y colección.

La persistencia sobrevive a cerrar/reabrir y a recrear la actividad; una pesca en curso se reinicia al recrearse la actividad. Borrar datos o desinstalar elimina el progreso local. Los avisos usan AnimatedVisibility, los indicadores animateColorAsState y animateDpAsState, la navegación y los estados AnimatedContent; el hilo usa Animatable y el paisaje se dibuja con Canvas.

## Compilar y comprobar

En Windows, desde la raíz del proyecto:

```bat
gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
```

En el entorno cloud preparado:

```sh
. /workspace/toolchains/activate.sh
bash gradlew --no-daemon --max-workers=4 assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas JVM comprueban compras, equipos, todas las profundidades/cañas, distribución de rarezas, recompensas duplicadas y récords. Las pruebas instrumentadas comprueban la navegación y la persistencia entre instancias sin tocar la partida real (preferencias de prueba separadas).

## Validación manual en Pixel 6 / API 34

Las pruebas instrumentadas necesitan un emulador/dispositivo; compilarlas no equivale a ejecutarlas:

```bat
gradlew.bat connectedDebugAndroidTest
```

- Abrir los cuatro ejercicios, jugar y volver al laboratorio con botones y Atrás del sistema.
- Comprobar nubes, brazos/caña, hilo, entrada al agua, natación, picada, recogida y transiciones de éxito/fallo; evaluar fluidez en el dispositivo.
- Acertar/fallar y pulsar repetidamente ATRAPAR: una captura paga una sola vez.
- Comprar hasta quedarse sin saldo, equipar otra caña y verificar límites del deslizador y bloqueos de zonas.
- Cerrar/reabrir y rotar: monedas, compras, equipo y récords deben mantenerse. Una pesca incompleta puede reiniciarse.
- Ir al segundo plano durante picada/recogida y volver; confirmar que no se duplican recompensas.
- Revisar filtros, siluetas y brillo legendario; probar también fuente grande y orientación horizontal.

No se ha ejecutado un emulador ni se ha medido rendimiento en dispositivo en este entorno; no tiene aceleración KVM disponible.
