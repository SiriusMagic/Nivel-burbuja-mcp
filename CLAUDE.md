# Nivel (nivel de burbuja MCP): reglas para agentes

- Lee `ARQUITECTURA.md` y abre solo los archivos que necesites.
- Pruebas: `cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleRelease` (debe pasar todo).
  Nunca desactives ni saltes una prueba. Lint sin avisos nuevos.
- Cada versión: sube `versionCode` y `versionName`, copia el APK a `android/Nivel-<versión>.apk` (borra el anterior)
  y anótalo en `CHANGELOG.md`.
- Matemáticas puras (ángulos desde la gravedad, calibración, unidades de pendiente, formato español, plomada,
  inclinómetro, regla mm↔px) en `data/`, sin Android y con pruebas. Las pantallas solo pintan y avisan.
- Privacidad: ajustes y mediciones viven solo en el teléfono (fuera de copias). La cámara solo dibuja el horizonte y no
  guarda imágenes. Nada sale salvo por el Modo MCP que el usuario active; internet es solo para eso.
- Las herramientas MCP solo leen. Cada herramienta nueva lleva su prueba en `McpTest`: lista de herramientas, llamada
  correcta, argumentos malos y «no encontrado».
- Prohibido: `eval`, motores de scripts, SQL concatenado.
- Firma: la de depuración del entorno, igual que Stone, Luma, Calculadora, Notas, Filem y Gateway Hub.
