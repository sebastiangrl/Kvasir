# Spec 004 — Overlay scrubber A–Z

**Estado:** aprobada  
**Rama:** `spec/004-overlay-scrubber`

## Problema

Home ya es hiperfocus (favoritas + CTA). Falta el escape hatch del dominio: ver y lanzar **todas** las apps lanzables vía overlay A–Z, sin ensuciar Home, sin iconos y sin ensanchar Ajustes como único catálogo de lanzamiento.

## Fuera de alcance

Hábitos (005), tema persistido (006), búsqueda, iconos/`Bitmap`, carpetas, long-press para marcar favorita, work profile, gestos extra de v2. No cambiar contrato HOME, `<queries>`, permisos ni esquema DataStore. Overlay **no** sustituye el catálogo alta/baja de favoritas en Ajustes (003).

**Decisiones de producto:**

1. **Abrir** — gesto swipe-up desde el contenido de Home.
2. **Cerrar** — Back, swipe-down, o tras lanzar una app. `onNewIntent` (volver a Home) deja el overlay cerrado.
3. **Contenido** — lista de texto de todas las `InstalledApp` (repo 002); tap = lanzar. No añade ni quita favoritas.
4. **Scrubber** — letras A–Z + `#` (labels cuya letra inicial no es A–Z según locale). Arrastre/selección filtra por letra inicial del `label`. Letra sin apps → empty.
5. **Capas** — overlay Compose perezoso (`ui/overlay/`); al cerrar, soltar pointer/estado efímero. Sin permiso de overlay de sistema.

## Requisitos (EARS)

### RF-004-01 — Abrir

Cuando el usuario haga swipe-up desde el contenido de Home, el sistema debe mostrar el overlay de apps.

### RF-004-02 — Cerrar

Cuando el usuario pulse Back, haga swipe-down en el overlay, o lance una app desde el overlay, el sistema debe cerrar el overlay. Cuando llegue `onNewIntent` (vuelta a Home), el sistema debe dejar el overlay cerrado.

### RF-004-03 — Lista y lanzar

El sistema debe listar en el overlay todas las `InstalledApp` ya en memoria (mismo repositorio que 002), solo texto (label), y cuando el usuario pulse una fila debe lanzarla vía ViewModel/repositorio (sin `LauncherApps` en Composables).

### RF-004-04 — Scrubber

El sistema debe ofrecer un scrubber con letras A–Z y `#`; al seleccionar o arrastrar, debe filtrar la lista a apps cuyo `label` (locale) empieza por esa letra; `#` agrupa labels que no empiezan por A–Z.

### RF-004-05 — Vacío por letra

Mientras la letra seleccionada no tenga apps, el sistema debe mostrar el empty `Ninguna app con esta letra.` sin inventar filas.

### RF-004-06 — Sin mutar favoritas

El sistema no debe añadir ni quitar favoritas desde el overlay; la alta/baja permanece en Ajustes.

### RF-004-07 — Capas

Los `@Composable` no deben llamar a `LauncherApps`, `PackageManager` ni DataStore; observan ViewModel/repos.

### RF-004-08 — Pereza y rendimiento

El sistema debe crear el overlay de forma perezosa; al cerrarlo, debe soltar el pointer/estado del scrubber (`ScrubberState` efímero, no persistido). Debe respetar el presupuesto de `docs/domain.md`: scrubber ~60 fps / p95 &lt; 16 ms, RSS idle &lt; 80 MB con overlay cerrado, cero bitmaps de iconos, sin degradar cold start / `onNewIntent`.

## Copy de UI

- Empty por letra: `Ninguna app con esta letra.`
- Sin cabecera obligatoria tipo «Apps»; el scrubber es el affordance principal.

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Overlay perezoso; cerrado no retiene scrubber pointer ni lista Compose montada de más.
- Lista = strings ya en memoria; sin iconos/`Bitmap`.
- Reloj aislado a 1 Hz; abrir/cerrar overlay no debe forzar recomposición del tick del reloj de forma innecesaria.
- Scrubber en arrastre: objetivo 60 fps (p95 &lt; 16 ms) según dominio.
- RSS idle (overlay cerrado) **&lt; 80 MB**; `onNewIntent` / cold start metas de `docs/domain.md` intactas.

## Persistencia local

Sin cambio de esquema. `ScrubberState` es efímero (memoria UI).

## Contrato con Android

Sin cambio de Manifest, permisos ni intents. **No** `SYSTEM_ALERT_WINDOW` ni overlay de sistema: el overlay es UI in-process sobre la Activity HOME.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-004-01 | | | Swipe-up abre overlay |
| RF-004-02 | | | Back / swipe-down / lanzar cierra; `onNewIntent` cierra |
| RF-004-03 | | | Lista completa; tap lanza |
| RF-004-04 | Filtro por letra / `#` (puro) | | Scrubber filtra en dispositivo |
| RF-004-05 | Empty sin apps para letra | | Empty copy visible |
| RF-004-06 | | | Overlay no escribe favoritas |
| RF-004-07 | | | Revisión: Compose sin PM/`LauncherApps`/DataStore |
| RF-004-08 | | | Overlay perezoso; cerrar suelta estado; sin iconos |

## Criterio de hecho

- Swipe-up abre overlay; Back / swipe-down / lanzar / `onNewIntent` lo cierran.
- Lista A–Z (+ `#`) en texto; tap lanza; scrubber filtra; empty por letra vacía.
- Overlay no muta favoritas; Ajustes intacto para alta/baja.
- Sin deps nuevas, sin Manifest/DataStore nuevos, sin bitmaps.
- Presupuesto de rendimiento de `docs/domain.md` respetado.
