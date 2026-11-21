# Nivel (nivel de burbuja MCP)

Nivel de burbuja para Android, con fondo azul marino de curvas de nivel y acento lima. Parte de la familia de Stone,
Luma, Calculadora, Notas y Filem: tu IA (Claude, ChatGPT…) puede **leer la inclinación del teléfono y tus mediciones** por
MCP.

## Estado (0.1.0)

- **Nivel:** tubo horizontal (X), tubo vertical (Y) y ojo de buey con anillos y cruz; burbujas brillantes que se mueven
  con la gravedad suavizada y se ponen en color de acento dentro de ±0,5°. Lecturas «X: 02,8°» y «Y: 01,5°», botón
  **Calibrar** y linterna.
- **Plomada:** una pesa que siempre cuelga hacia abajo, con un pequeño balanceo, y el desvío grande arriba («-0,9°»).
- **Inclinómetro (Ángulo):** inclinación de la superficie, diana y giro «< -0,9° >»; referencia cara frontal o trasera.
- **Regla:** cm o pulgadas a tamaño real (densidad real de la pantalla), en los dos bordes largos; sirve acostado.
- **Cámara:** horizonte sobre la imagen de la cámara con el ángulo («-9°»). Pide el permiso con explicación.
- **Guardar medición** y **Mediciones guardadas** (en el teléfono), con eliminar.
- **Ajustes:** Restablecer calibración, Mantener la pantalla encendida, Unidad de pendiente (°, %, mm/m), Sonido de
  nivelación, Temas (Sistema, Claro, Oscuro, Negro y color de acento) y **Modo MCP**.
- **Modo MCP:** `level_reading`, `level_calibration` y `level_measurements` (solo lectura). Mismo relay y Gateway Hub
  (módulo `level_`) que las demás apps.
- Sin anuncios ni estadísticas; internet solo para el Modo MCP (apagado de fábrica).

APK: [`android/Nivel-0.1.0.apk`](android/Nivel-0.1.0.apk) (`com.zirakusa.nivel`, firmado como Stone y Gateway Hub).

## Compilar y probar

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

## Siguiente

Ver [`ROADMAP.md`](ROADMAP.md).
