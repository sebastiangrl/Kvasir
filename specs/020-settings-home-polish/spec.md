# Spec 020 — Pulido Home/Ajustes + acceso a notificaciones

**Estado:** en implementación  
**Rama:** `spec/020-settings-home-polish`

## Problema

Tras 019 el Home es usable, pero el usuario nota holguras de producto:

1. El día (TUE) no tiene la tipografía geométrica de referencia; además el bloque reloj/evento **no está centrado** respecto al buscador (los `weight` de hora/fecha desplazan el día).
2. «Ajustes» como texto compite con el reloj; un **icono** basta.
3. Los iconos de apps se ven **raster sucio** (filtro blanco sobre bitmap pequeño), no como trazo nítido.
4. El scrubber ya curva, pero falta el **deslizamiento continuo** de la burbuja entre letras y un **toque háptico** al cambiar de slot.
5. En Ajustes, «Atrás» es un TextButton raro; Pomodoro solo guarda con IME Done campo a campo; Favoritas no se pueden buscar; Hábitos son campos + «Eliminar» en texto, sin iconos.
6. Al activar **acceso a notificaciones** (badges 017) Android 13+ muestra *Restricted setting* en APK sideload: no es un crash, pero el hub no explica el desbloqueo.

## Fuera de alcance

- Copiar assets/tipografía de Niagara.
- Anurati **de pago / Pro**; icon packs; Coil/Glide; Material Icons Extended como dep nueva si se puede con vectores propios.
- Quitar badges ni el listener (017).
- Play Store / instalación session-based automática.
- Per-app theming (descartado).
- Reordenar favoritas con drag.

**Decisiones de producto:**

1. **Día** — fuente display **Anurati** (Emmeran Richard) **solo** para la abreviatura del día. Uso **personal** (no comercialización); crédito al autor en docs/README o comentario de recursos. El resto de Home sigue con tipografía Material.
2. **Centrar** — el **día** es el eje óptico del bloque (centrado en el ancho de contenido, no empujado por hora/fecha). Hora izquierda y fecha derecha se superponen/alinean a ese eje (Box), no `Row`+`weight`.
3. **Ajustes Home** — IconButton (engranaje) con contentDescription «Ajustes». Vectores en `res/drawable`, sin librería de iconos.
4. **Silueta nítida** — rasterizar el glifo a **≥2×** el tamaño de pantalla y extraer **máscara por luminancia/alfa** (trazo), no tintar el bitmap de color a 28 dp. Sigue prohibido Coil y cache Bitmap ilimitada.
5. **Scrubber** — burbuja con **offset interpolado** (no salto de celda); háptica corta al **cambiar de índice** (`HapticFeedback`; `VIBRATE` solo si hace falta). Idle sigue compacto.
6. **Ajustes chrome** — atrás = flecha IconButton; title al lado.
7. **Pomodoro** — borradores locales + botón **Guardar cambios**; no exigir Done por campo.
8. **Favoritas (Ajustes)** — campo «Buscar apps» (píldora 019) filtra por label; no muta DataStore hasta el switch.
9. **Hábitos (Ajustes)** — alta/baja/guardar con **iconos** (añadir, eliminar); menos texto de acción.
10. **Restricted settings** — copy en Permisos + CTA a ficha de la app; no se puede saltar el diálogo del SO.

## Requisitos (EARS)

### RF-020-01 — Fuente del día

El sistema debe mostrar la abreviatura del día de la semana con la fuente **Anurati**. El resto de Home (hora, fecha, listas) no debe usar esa fuente. El APK o la documentación deben acreditar a Emmeran Richard.

### RF-020-02 — Bloque reloj centrado

El sistema debe centrar ópticamente el día (y el evento debajo) en el ancho de contenido de Home; hora y fecha flanquean sin desplazar ese eje.

### RF-020-03 — Icono de Ajustes

Cuando Home esté visible, el sistema debe ofrecer Ajustes como icono (no la palabra «Ajustes»), con descripción de accesibilidad en español.

### RF-020-04 — Iconos de apps nítidos

El sistema debe pintar los iconos de apps como **silueta de trazo** nítida a la densidad de pantalla (no un disco/filtro a baja resolución). Si no hay drawable, hueco vacío.

### RF-020-05 — Burbuja continua + háptica

Mientras el usuario arrastre el scrubber, la burbuja debe **deslizarse** a lo largo del rail (posición continua). Cuando el índice de letra/★ cambie, el sistema debe emitir un feedback háptico breve. Si el dispositivo no tiene vibración, el gesto sigue funcionando.

### RF-020-06 — Atrás en Ajustes

El sistema debe mostrar el volver atrás de Ajustes como **flecha** (icono), no como TextButton «Atrás».

### RF-020-07 — Guardar Pomodoro

En la sección Pomodoro de Ajustes, el sistema debe permitir editar trabajo/descanso/sesiones y **persistir solo** al pulsar «Guardar cambios» (los tres campos a la vez). IME Done no es el único camino.

### RF-020-08 — Buscar en Favoritas

En la sección Favoritas de Ajustes, el sistema debe filtrar la lista por substring de `label` con un campo de búsqueda. Query vacía = lista completa.

### RF-020-09 — Hábitos con iconos

En la sección Hábitos de Ajustes, el sistema debe usar iconos para añadir y eliminar (y guardar/confirmar el alta). El rename puede seguir en el campo.

### RF-020-10 — Acceso a notificaciones (sideload)

Si el acceso a notificaciones está denegado, el sistema debe explicar en español que en Android 13+ (APK sideload) hay que **Permitir ajustes restringidos** en la ficha de Kvasir y luego activar el listener. Debe ofrecer abrir la ficha de la app **y** la pantalla de acceso a notificaciones.

### RF-020-11 — Capas y deps

`@Composable` no llama a `LauncherApps` / `PackageManager` / DataStore. Sin Coil/Glide. Vectores de UI propios. `VIBRATE` solo si la háptica del framework no basta (permiso normal).

## Copy de UI

- contentDescription Ajustes: `Ajustes`
- contentDescription atrás: `Atrás`
- Pomodoro: `Guardar cambios`
- Permisos (listener): `En Android 13 o superior, si el APK no viene de Play Store: Ajustes del sistema → Apps → Kvasir → menú ⋮ → Permitir ajustes restringidos. Luego activa el acceso a notificaciones.`
- CTA extra: `Abrir ficha de la app` (reutilizar abrir detalles si aplica)
- Hábitos: contentDescription `Añadir`, `Eliminar`

## Impacto en rendimiento

Esta spec **toca el proceso Home** (reloj, iconos, scrubber, Ajustes en el mismo proceso).

- **RAM:** un `.otf` pequeño (~decenas de KB); siluetas a 2–3× 28 dp, LRU 48 intacta. RSS idle &lt; 80 MB.
- **Recomposiciones:** reloj aislado; háptica/arco solo en el rail; búsqueda de Favoritas local en Ajustes (no tick 1 Hz).
- **Arranque:** fuente display lazy al componer HomeClock. Metas cold start / `onNewIntent` intactas.
- **Scrubber:** interpolación de burbuja sin recomponer la lista de apps (letra estable como 013/019).

## Persistencia local

Sin cambio de esquema. Pomodoro sigue las mismas claves; solo cambia **cuándo** se escribe (al Guardar). Compatibilidad: valores actuales se muestran como borrador.

## Contrato con Android

- Manifest: `VIBRATE` **solo si** el plan lo exige (permiso normal). Sin nuevos peligrosos.
- Intents: reutilizar ficha de app (009/016) + `ACTION_NOTIFICATION_LISTENER_SETTINGS` (017).
- No se puede desactivar Restricted settings por código.

## Dependencias

Sin librerías nuevas. Fuente: un archivo en `res/font` (tamaño vs beneficio: un Text del día). Vectores XML propios.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-020-01 | — | — | Día usa display; hora no |
| RF-020-02 | — | — | Día alineado al eje del buscador |
| RF-020-03 | — | — | Engranaje abre Ajustes |
| RF-020-04 | silueta/luminancia JVM si hay ops | — | WhatsApp/Brave nítidos, no mancha |
| RF-020-05 | — | — | Burbuja se desliza; click háptico al cambiar letra |
| RF-020-06 | — | — | Flecha vuelve hub/Home |
| RF-020-07 | — | — | Editar tres campos → Guardar persiste |
| RF-020-08 | filtro label (reusar AppLabelFilter) | — | Buscar recorta la lista |
| RF-020-09 | — | — | + y papelera/icono borrar |
| RF-020-10 | — | — | Copy visible; ficha + listener |
| RF-020-11 | — | — | Compose sin PM; Gradle sin Coil |

## Criterio de hecho

- Reloj centrado; Ajustes/atrás con iconos; siluetas nítidas; scrubber con burbuja continua + háptica; Pomodoro Guardar; Favoritas con búsqueda; Hábitos con iconos; copy Restricted settings.
- Presupuesto Home intacto; PR único `spec/020-settings-home-polish`.
