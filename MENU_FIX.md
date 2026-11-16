# Corrección del menú y controles

## Hallazgos y cambios

- Los cinco `onClick` del menú ya estaban conectados al estado `pantalla`, almacenado con `rememberSaveable`. No se encontró `pointerInput`, un Canvas superpuesto ni otro interceptor de eventos en ese menú. Sin una reproducción válida en dispositivo, no se atribuye el bloqueo de pulsaciones a una causa que no se haya demostrado.
- El menú anterior dimensionaba sus botones al contenido y no establecía altura ni ancho de lectura. Ahora usa un encabezado separado y una `LazyColumn` con espacio acotado: botones de ancho completo, mínimo 88 dp, separación, descripciones y ancho máximo 560 dp.
- Varios títulos grandes cambiaban `fontSize` manteniendo una altura de línea heredada menor. Esto puede comprimir o solapar texto. Se establecieron estilos y alturas de línea explícitos, incluidos los títulos de 42 sp del juego (ahora 50 sp de interlínea).
- Los botones compartidos del juego tienen mínimo 56 dp, texto de 16 sp, interlínea de 22 sp y padding que permite crecer con el texto. Se corrigió también la distribución de los encabezados.
- La actividad tiene una `Surface` y el tema del proyecto explícitos. Los cuatro ejercicios se conservan; su contenido puede desplazarse y sus botones de regreso tienen mínimo 56 dp. Se mantiene Atrás del sistema y el paquete original.

## Verificación realizada

En Linux se ejecutó el equivalente del wrapper Windows:

```sh
. /workspace/toolchains/activate.sh
bash gradlew --no-daemon --max-workers=4 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

Resultado: compilación y APK de pruebas correctos; siete pruebas JVM aprobadas; lint sin errores.

`FishingNavigationTest` ahora inyecta pulsaciones por coordenadas (`performTouchInput`) en lugar de invocar únicamente las acciones semánticas. Comprueba los tamaños del menú, las acciones de los cuatro ejercicios, la entrada al juego y la navegación a tienda, colección, zonas y lanzamiento de la caña.

## Límite de la comprobación en emulador

Se preparó un AVD Android 14/API 34 con resolución 1080 × 2400 y densidad 420 (perfil Pixel 6), ejecutado por software porque no hay KVM. Se lanzó `connectedDebugAndroidTest`. La primera instalación falló por `INSTALL_FAILED_VERIFICATION_FAILURE` al agotarse el tiempo de verificación. Ampliar el plazo de integridad de 30 a 120 segundos, sin desactivar la verificación, permitió instalar el APK.

Sin embargo, el sistema Android del emulador sufrió reinicios de servicios, tiempos de apertura agotados y mostró **Process system isn't responding** sobre su launcher. Se detuvo el emulador y la ejecución conectada porque ese entorno no permite comprobar fiablemente la interacción. El informe final de la ejecución conectada contiene cero pruebas completadas y termina con `device offline` tras detener el emulador. No se consideran aprobadas las pruebas instrumentadas ni verificados los botones en dispositivo. Los errores observados en servicios del sistema no se presentan como la causa del problema reportado por el usuario.

Para completar la validación en un Pixel 6/API 34 con aceleración y Android estable:

```bat
gradlew.bat connectedDebugAndroidTest
```

Comprobar también fuentes grandes, orientación horizontal, Atrás del sistema y el minijuego. Si persiste un bloqueo, se requiere el Logcat de esa ejecución para establecer su causa; el análisis estático no encontró callbacks ausentes ni una capa táctil bloqueante en el menú.
