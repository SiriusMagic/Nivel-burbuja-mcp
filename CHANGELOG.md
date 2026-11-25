# Nivel 0.3.0

- **El Modo MCP lo controla Arka**: se quita la pantalla «Modo MCP» (Activar MCP, Redireccionar a Hub, Crear token y la
  lista de tokens). Activarlo, permitirlo en local y crear tokens se hace desde **Arka › MCP**; Ajustes › Modo MCP solo
  muestra el estado y abre Arka. Si ya estaba conectada al hub, sigue conectada con el mismo enlace; los tokens LLM y Hub
  creados aquí dejan de funcionar (créalos en Arka).
- Lucía usa las herramientas en el teléfono aunque el enlace público esté apagado; lo decide «Permitir MCP local» en Arka.
- App más ligera: 1,99 MB → 1,78 MB.

# Nivel 0.2.0

- **Iconos nuevos**: el nivel de burbuja en cinco estilos, **Pixelado** (el de fábrica), Realista, Acuarela, Neón y
  Dibujo, en Ajustes › Icono de la app. Se quitan los anteriores (Original, Claro, Dorado, Azul y Verde); si tenías uno
  elegido, pasa solo al nuevo que ocupa su lugar.

# Nivel 0.1.1

- **Conectar todas** (Gateway Hub 0.0.21): con un toque en el Hub se activa aquí «Redireccionar a Hub» y la app queda
  vinculada, sin entrar en ella. Solo lo puede pedir una app firmada por el mismo desarrollador.
- **Lucía** (la voz del Hub) usa las herramientas de esta app en el teléfono, **sin internet**, con las mismas reglas que
  por MCP. Solo mientras «Redireccionar a Hub» esté activado.
- **Icono de la app**: 5 iconos para elegir (Original, Claro, Dorado, Azul y Verde) en Ajustes › Icono de la app. Tras esta
  actualización quizá tengas que volver a poner el icono en tu pantalla de inicio (una sola vez).

# Nivel 0.1.0

Primera versión: un nivel de burbuja completo, azul marino con curvas de nivel y acento lima.
- **Nivel:** tubos horizontal y vertical y ojo de buey; burbujas brillantes con el sensor de gravedad (o acelerómetro)
  suavizado; color de acento dentro de ±0,5°; lecturas «X: 02,8°» / «Y: 01,5°» en monoespaciada con coma decimal;
  **Calibrar** (cero en la superficie actual); linterna.
- **Plomada:** pesa con balanceo amortiguado sobre arcos punteados; desvío de la vertical en grande.
- **Inclinómetro:** inclinación con flechas ∧ ∨, diana con el borde girado y giro «< -0,9° >»; Referencia cara frontal /
  cara trasera.
- **Regla:** cm (marca cada mm, número cada cm) o pulgadas (cada 1/16″) con `xdpi`/`ydpi`; en los dos bordes largos.
- **Cámara:** vista previa (Camera2) con cruz blanca y horizonte lima girado por el giro del teléfono, ángulo y cerrar;
  permiso de cámara pedido con explicación.
- **Guardar medición** (Nivel, Plomada, Inclinómetro) y **Mediciones guardadas** con estado vacío y eliminar.
- **Ajustes:** Restablecer calibración, Mantener la pantalla encendida, Unidad de pendiente (Grados ° / Porcentaje % /
  mm/m), Sonido de nivelación, Temas (Sistema, Claro, Oscuro, Negro; Lima, Amarillo, Naranja, Rosa, Azul, Verde),
  privacidad y versión.
- **Modo MCP**, igual que Stone, Luma, Calculadora y Notas: Activar MCP, tokens LLM o Hub (`niv_…`), pausar, revocar y
  Redireccionar a Hub. Herramientas de solo lectura `level_reading`, `level_calibration` y `level_measurements`; en el hub
  aparece como `level_`.
- Ajustes y mediciones fuera de las copias en la nube. Internet solo para el Modo MCP (apagado de fábrica).
- 22 pruebas.
