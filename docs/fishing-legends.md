# Fishing Legends: arrastre y exploración

## Controles y simulación

El anzuelo desciende automáticamente. Arrastra horizontalmente dentro de la escena del agua,
con el dedo o con el botón izquierdo del mouse presionado. El arrastre desplaza un objetivo
relativo al anzuelo: no salta a la posición inicial del toque. Un controlador suavizado limita
su velocidad según la caña y mantiene X entre 2 y 98 de un mundo de 100 unidades.
Soltar o cancelar el gesto elimina el objetivo. El área de arrastre no cubre Puerto, RECOGER
ni los diálogos. RECOGER sigue siendo una pulsación mantenida y admite un segundo dedo
mientras se dirige el anzuelo con el primero. Con un mouse se puede dirigir, soltar y recoger.

La simulación Kotlin es independiente de Compose. Consume el tiempo transcurrido con pasos
fijos de 1/120 segundos; el render muestra instantáneas por fotograma. Una colisión barrida
relativa entre pez y anzuelo elige el primer contacto y engancha un solo pez. Peces y anzuelo
usan las mismas coordenadas de mundo para movimiento y dibujo, con cámara vertical.
El límite de profundidad es siempre el mínimo entre el alcance de la caña y el de la zona.

Los comunes nadan normalmente. Los veloces cambian de dirección cada 1,6–2,3 segundos.
Los asustadizos detectan el anzuelo a 14 unidades horizontales/4 metros verticales; huyen
0,8 segundos y descansan hasta 3,6 segundos desde la detección. Nunca desaparecen por miedo.
Los gigantes son más lentos; su tamaño aumenta la resistencia al recogerlos. Los legendarios
bailan horizontalmente y oscilan en profundidad, dentro del rango legal de su especie.

Mantener RECOGER eleva el pez y la tensión; soltar reduce la tensión y permite retroceso.
Permanecer 1,2 segundos al 100 % rompe el hilo. La captura conserva el tamaño real del ejemplar
que nadaba en el escenario. Cada lance tiene un recibo único y los premios son idempotentes.

Pausar libera todos los controles y congela el reloj. Reanudar exige un gesto nuevo.
Atrás/Puerto pide cancelar una captura en curso. La recreación de la actividad cierra la sesión
volátil; conserva lo guardado y muestra un aviso. Los cuatro ejercicios y las animaciones
AnimatedVisibility, animateColorAsState, animateDpAsState y AnimatedContent se conservan.

## Especies especiales

El catálogo conserva las 12 especies anteriores y añade cuatro ilustraciones vectoriales
originales. Las condiciones de aparición se comprueban en cada nuevo lance:

| Especie | Zona / profundidad | Nivel | Recompensa especial |
| --- | --- | --- | --- |
| Koi del sol | Lago tranquilo, 2–25 m | 1 | Monedas dobles |
| Tetra de cristal | Lago tranquilo, 6–25 m | 3 | XP triple |
| Sombra de las raíces | Lago profundo, 42–100 m | 8 | 80 monedas adicionales |
| Fénix de marea | Océano, 175–200 m | 20 | 250 monedas adicionales y XP doble |

El Leviatán existente también recibe el botín legendario. Dorados llevan destellos y un
contorno dorado, cristalinos tienen facetas translúcidas, secretos lucen marcas luminosas
y legendarios muestran aura y cresta. Todos se registran en la colección con tamaño y cantidad.
No se usan emojis ni imágenes de referencia.

## Cañas y niveles

Se conservan precios, IDs, seis cañas y alcances: 10, 25, 50, 100, 200 y 500 metros.
Para una caña con índice i (0–5): control = 1 + 0,10i, resistencia = 1 + 0,12i y velocidad de
recogida = 1 + 0,10i. Control aumenta la velocidad lateral máxima y el seguimiento; resistencia
reduce la tensión generada por la lucha; recogida multiplica la velocidad de ascenso.
La tienda muestra estos valores. Comprar exige monedas y nivel. Equipar una caña poseída
exige su nivel, sin cobrarla de nuevo. Una caña antigua ya equipada permanece equipada;
no se elimina del inventario. Una zona exige tanto nivel como alcance suficiente.

XP base: común 25, poco común 50, rara 125 y legendaria 350, más las bonificaciones indicadas.
El nivel L requiere `50 × L × (L − 1)` XP totales; el siguiente nivel cuesta `100 × L` XP.
Los desbloqueos permanecen: madera/lago tranquilo 1, básica 3, lago profundo 5, profesional 8,
titanio 12, océano 15, eléctrica 20, abismo 25 y legendaria 30.

## Misiones, récord y persistencia

Tres misiones sencillas se pagan automáticamente una sola vez:

- Cinco capturas nuevas: 100 monedas y 75 XP.
- Tres especies en capturas nuevas: 150 monedas y 125 XP.
- Alcanzar 40 metros: 200 monedas y 150 XP.

El récord mide profundidad alcanzada, incluso sin captura. Se guarda en checkpoints de diez
metros y con precisión entera al pausar, terminar o salir normalmente. El cierre abrupto del
proceso puede perder lo avanzado desde el último checkpoint. Cancelar una captura no la premia;
los logros de exploración y el récord sí se conservan.

Se mantiene la preferencia `fishing_legends_v1`, entrada `progress`, y todos los campos previos.
El formato 3 añade `mission_catches`, `mission_species`, `mission_deepest` y `completed_missions`.
Partidas v1/v2 conservan monedas, compras, equipo, XP existente, colección, récord y recibos.
Las misiones nuevas empiezan a cero: no se pagan por registros históricos. El formato ampliado
se guarda en una sola entrada mediante commit y un bloqueo compartido entre instancias.
No se borran preferencias ni se ejecutan reinicios de progreso en la aplicación.

## Pruebas y comprobación en Android Studio

```sh
source /workspace/.cloud-setup/android-env.sh
cd /workspace/Lab13Animaciones
bash gradlew --no-daemon --max-workers=4 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

En Android Studio, actualizar `feature/fishing-legends`, sincronizar Gradle y ejecutar `app`
en Pixel 6/API 34. Verificar:

1. Abrir el laboratorio, sus cuatro ejercicios, tienda, colección y zonas.
2. Lanzar y arrastrar con clic izquierdo sobre el agua: movimiento suave, límites laterales,
   descenso automático y cámara. Puerto debe seguir respondiendo.
3. En dispositivo táctil, arrastrar con un dedo y mantener RECOGER con otro; cancelar gestos.
   Con mouse, dirigir, soltar y mantener RECOGER. Observar tensión y lucha del pez.
4. Perseguir peces asustadizos, observar giros de rápidos y la danza legendaria sin huidas perpetuas.
5. Comprobar las cuatro nuevas especies según sus condiciones, sus efectos y sus premios.
6. Revisar estadísticas de cañas y rechazos de compras/equipamiento/zonas sin nivel o alcance.
7. Completar misiones, salir y reabrir: no debe repetirse el premio. Descender sin capturar y
   volver al puerto: el récord debe actualizarse. Abrir un guardado antiguo sin borrar datos.
8. Pausar o rotar durante un arrastre: no debe quedar activo ni crear una captura automática.
9. Ejecutar `:app:connectedDebugAndroidTest` con dispositivo conectado para las pruebas de UI
   y SharedPreferences. Compilar el APK de pruebas no equivale a ejecutarlas.

## Validación en la nube

`assembleDebug`, `testDebugUnitTest`, `lintDebug` y `assembleDebugAndroidTest`: correctos.
30 pruebas JVM ejecutadas: 0 fallos, 0 errores, 0 omitidas. Lint: 0 errores y 13 advertencias.
No se ejecutaron las pruebas instrumentadas: esta instancia no dispone de emulador operativo
ni /dev/kvm. La interacción real con mouse y el aspecto en Pixel 6/API 34 quedan pendientes
de la comprobación manual descrita arriba.

## Archivos de esta mejora

- `app/src/androidTest/java/com/tecsup/lab13animaciones/fishing/FishingNavigationTest.kt`
- `app/src/androidTest/java/com/tecsup/lab13animaciones/fishing/FishingStorageTest.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishMotion.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingCollection.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingGame.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingLogic.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingMissions.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingModels.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingScene.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingShop.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingSimulation.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingStorage.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/FishingWater.kt`
- `app/src/main/java/com/tecsup/lab13animaciones/fishing/MissionBoard.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingExpansionTest.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingLogicTest.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingProgressTest.kt`
- `app/src/test/java/com/tecsup/lab13animaciones/fishing/FishingSimulationTest.kt`
- `docs/fishing-legends.md`
