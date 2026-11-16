# Fishing Legends: exploración y recogida

La partida usa una simulación Kotlin independiente de Compose, con X en unidades de mundo
(0–100) y profundidad en metros. El lanzamiento dura 0,9 segundos. El anzuelo baja solo;
IZQUIERDA y DERECHA actúan desde que se presionan hasta que se sueltan o se cancela el gesto.
La cámara sigue al anzuelo y nunca se supera el alcance de la caña o la zona.

Los peces se generan como habitantes del mundo, respetando especie, zona y profundidad.
No se decide una captura antes del lance. La colisión usa el movimiento relativo del pez y
el anzuelo, barrido contra una elipse; gana el primer contacto y solo se engancha un pez.
La simulación consume todo el tiempo transcurrido en pasos de 1/120 segundos y comparte
posiciones y dimensiones del cuerpo con el dibujo. Los adornos usan el mismo reloj durante la partida.

RECOGER mantenido eleva el pez y la tensión. Soltarlo alivia la tensión y permite un pequeño
retroceso. La resistencia crece con la dificultad y rareza; el pez lucha lateralmente.
Permanecer 1,2 segundos seguidos al 100 % rompe el hilo. Llegar a superficie crea un único
recibo de captura; un fallo de escritura permite reintentar y bloquea un nuevo lance.

Al pausar la aplicación se liberan los controles y se congela el reloj. Al reanudar hay que
presionar de nuevo. Atrás/Puerto pregunta si se desea cancelar una sesión activa sin premios.
La recreación de la actividad cancela la sesión volátil y muestra un aviso; las capturas
confirmadas en almacenamiento se conservan. El cierre del proceso sigue la misma política:
no restaura sesiones incompletas ni genera capturas de oficio.

## Progreso y compatibilidad

XP por captura: común 25, poco común 50, rara 125, legendaria 350.
El total necesario para el nivel L es `50 × L × (L − 1)`; subir desde L cuesta `100 × L` XP.
La interfaz muestra nivel y progreso. Los desbloqueos son:

| Nivel | Desbloqueo |
| --- | --- |
| 1 | Lago tranquilo y madera |
| 3 | Caña básica |
| 5 | Lago profundo |
| 8 | Caña profesional |
| 12 | Caña de titanio |
| 15 | Océano |
| 20 | Caña eléctrica |
| 25 | Abismo legendario |
| 30 | Caña legendaria |

Comprar exige nivel y monedas. Una zona exige nivel y alcance mayor que su profundidad mínima.
El equipo comprado en partidas antiguas se conserva y puede equiparse aunque su nuevo requisito
sea superior al nivel del jugador.

Se mantienen `fishing_legends_v1`, la entrada `progress`, los IDs y los campos anteriores.
El formato ampliado añade `version`, `xp`, `level` y `reward_tokens`. El nivel se calcula de XP;
no se confía en un nivel guardado incoherente. Una partida antigua empieza con 0 XP sin perder
monedas, compras, equipo, colección ni récords. Su último recibo migra al historial. Un recibo
ya registrado, incluso anterior a otras capturas, no entrega monedas, XP ni registros de nuevo.
Todos estos datos se escriben juntos mediante commit; un bloqueo compartido serializa instancias.

## Validación y comprobación manual

```sh
source /workspace/.cloud-setup/android-env.sh
cd /workspace/Lab13Animaciones
bash gradlew --no-daemon --max-workers=4 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

Las pruebas JVM cubren movimiento, límites, contacto, enganche único, tensión, captura/escape,
FPS, recibos, compras, niveles, zonas y codificación/migración de partidas. Las pruebas instrumentadas
cubren preferencias reales, navegación del laboratorio y juego, pulsación/cancelación, pausa y
recreación. Compilar el APK de pruebas no significa ejecutarlas.

Esta instancia no dispone de emulador operativo ni /dev/kvm. En Pixel 6/API 34:

1. Recorrer los cuatro ejercicios, menú, tienda, zonas y colección; comprobar legibilidad y scroll.
2. Lanzar y mantener cada dirección, usar dos dedos y cancelar gestos fuera del botón.
3. Contactar visualmente con un pez: comprobar profundidad, hilo y cámara, sin selección anticipada.
4. Alternar RECOGER/soltar; observar subida, retroceso y lucha. Mantener 100 % para provocar escape.
5. Llegar a superficie y verificar monedas, XP y colección una sola vez; volver a abrir la aplicación.
6. Pausar manteniendo un botón y reanudar: no debe quedar presionado ni avanzar durante la pausa.
7. Usar Atrás, descartar/confirmar cancelación y rotar: no deben aparecer premios de sesiones canceladas.
8. Abrir una partida v1 con equipo avanzado: comprobar conservación y bloqueos nuevos de nivel.
9. Ejecutar `:app:connectedDebugAndroidTest` con un dispositivo de pruebas conectado.

## Archivos de esta implementación

- `app/build.gradle.kts`
- `app/src/androidTest/java/com/tecsup/lab13animaciones/fishing/FishingNavigationTest.kt`
- `app/src/androidTest/java/com/tecsup/lab13animaciones/fishing/FishingStorageTest.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/DiveScene.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingCollection.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingGame.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingLogic.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingModels.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingScene.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingShop.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingSimulation.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingStorage.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingWater.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingLogicTest.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingProgressTest.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingSimulationTest.kt`
- `docs/fishing-legends.md`
- `gradle/libs.versions.toml`

## Resultado de validación en la nube

`assembleDebug`, `testDebugUnitTest`, `lintDebug` y `assembleDebugAndroidTest`: correctos.
19 pruebas JVM ejecutadas: 0 fallos, 0 errores, 0 omitidas.
Lint: 0 errores y 13 advertencias (no se deshabilitaron comprobaciones).
Pruebas instrumentadas: compiladas, no ejecutadas por ausencia de dispositivo/emulador.
