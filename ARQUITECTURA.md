# Arquitectura de Nivel

Carpeta = package (`android/app/src/main/java/com/zirakusa/nivel/`):

| Carpeta | Qué hay |
|---|---|
| `data/` | Lógica pura, con pruebas. `Tilt` y `Angles` (ángulos X/Y de cada eje desde el vector de gravedad, giro de la cámara, ejes según la rotación de la pantalla, posición de la burbuja), `LowPass` (suavizado); `Slope` (`SlopeUnit` grados/%/mm por m y `Format`: «02,8°», «4,9 %», «-0,9°» con coma decimal); `Plumb` (desvío de la plomada, inclinación y giro del inclinómetro por cara frontal o trasera) y `Pendulum` (balanceo amortiguado); `Ruler` (mm y pulgadas ↔ píxeles con `xdpi`/`ydpi`, marcas); `Settings` (ajustes tipados sobre `KeyValues`; `SharedValues` = preferencias «nivel») y `Measurements` (`MeasurementStore`: «Guardar medición», JSON en las mismas preferencias, máx. 500). |
| `ui/` | `LevelState` (único dueño del estado: gravedad suavizada en ejes de pantalla y todo lo que se mide con ella, calibración, ajustes, mediciones, pestaña y página) y `NivelApp` (barra inferior: Nivel, Plomada, Ángulo, Regla, Cámara, Ajustes; páginas Modo MCP y Mediciones guardadas). |
| `ui/screens/` | `LevelScreen` (tubos, ojo de buey, lecturas X/Y y «Calibrar»), `Vials` (dibujo de tubos y burbujas), `PlumbScreen`, `InclineScreen`, `RulerScreen`, `CameraScreen` (horizonte sobre la cámara y permiso), `MeasurementsScreen`, `SettingsScreen` y `SettingsParts` (tarjetas, interruptores, Temas) (igual que en las apps hermanas) y `Chrome` (botones redondos, fila superior, selector, fondo topográfico). Solo pintan y avisan. |
| `ui/theme/` | `Theme`: `ThemeMode` (Sistema, Claro, Oscuro azul marino, Negro puro), `Accent` (lima de fábrica, amarillo, naranja, rosa, azul, verde) y `LevelColors`, que todas las pantallas leen. |
| `mcp/` | Modo MCP, controlado por Arka (`HubLink`: `link`, `connect`, `disconnect`, `status`, `tool`): `Tokens` (`niv_…`, cifrados con `SecretBox`), `LevelTools` (`level_reading`, `level_calibration`, `level_measurements`; solo leen, a través de la interfaz `LevelData`), `McpHandler` (JSON-RPC, tokens, límites), `RelayLink` (relay de Stone), `McpMode` (estado e interruptores; `LiveReading` y `LiveData` con el sensor y las preferencias) y `HubLink` (enlace para Gateway Hub con firma; módulo `level_`). |
| `Platform.kt` | `TiltSensor` (sensor de gravedad o acelerómetro mientras la app está a la vista), `Torch` (linterna con `CameraManager.setTorchMode`) y `Beeper` (pitido de nivelación). |
| `CameraPreview.kt` | Vista previa de la cámara trasera con Camera2 y `TextureView` (sin CameraX); recorte centrado en vertical. |
| `MainActivity.kt` | Crea el estado, conecta el sensor en `onStart`/`onStop`, pantalla encendida, permiso de cámara con cortesía, copiar enlaces e «Ir al Hub». |

Reglas:
- Texto visible en `res/values/strings.xml` (español) y `values-en`.
- Colores siempre de `LevelColors` (nunca fijos en una pantalla, salvo el blanco de la cruz sobre la cámara). Encima del
  acento se usa `LevelColors.OnAccent`; para texto en acento sobre fondos claros, `LevelColors.AccentText`.
- Archivos de pantalla de unas 150 líneas como mucho; si crece, se divide.
- La burbuja va hacia el lado alto: X positivo = derecha, Y positivo = arriba. Nivelado = ambos ejes dentro de ±0,5°.
- `McpTest` usa un `LevelData` falso; `ScreenshotsTest` alimenta gravedad a mano y pone un degradado en lugar de la cámara.
