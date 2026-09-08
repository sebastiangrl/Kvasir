# Spec 013 — Home + scrubber unificado (Niagara)

**Estado:** aprobada  
**Rama:** `spec/013-niagara-home-scrubber`

## Problema

Hoy el escape hatch A–Z vive en un **overlay aparte** (004, swipe-up) y Home es solo favoritas. El usuario quiere el modelo **Niagara**: un solo Home con scrubber **★ + A–Z (+ `#`)**, donde ★ muestra favoritas y una letra muestra el listado de apps de esa letra — sin carpetas-grupo y sin saltar a otra “pantalla modal” de apps.

## Fuera de alcance

Carpetas-grupo de apps (descartado). Widgets, badges, work profile, atajos, rachas/historial, per-app theming. Reordenar favoritas con drag. Marcar/desmarcar favorita desde el listado A–Z (sigue en Ajustes). Cambiar contrato HOME / permisos calendario / tipografía del reloj salvo layout alrededor del scrubber. Icon packs.

**Decisiones de producto:**

1. **Una superficie** — el scrubber vive en **Home**. ★ = modo favoritas; letra / `#` = modo listado filtrado por inicial del `label` (locale), igual criterio que 004.
2. **Overlay 004** — el overlay a pantalla completa por swipe-up **deja de ser el camino principal** (y en esta spec se **retira** como superficie de listado A–Z). Swipe-up no debe abrir un segundo scrubber duplicado; el gesto puede quedar no-op o reasignarse en spec futura (p. ej. nada / panel). Default 013: **no abrir overlay de apps**.
3. **Modo ★** — conserva el Home hiperfocus: reloj (011), próximo evento (012), búsqueda sobre favoritas (008), lista de favoritas con iconos monocromos (010), hábitos (005), CTA Inicio si aplica. Scrubber visible a la derecha (o borde).
4. **Modo letra** — lista de apps de esa letra (icono monocromo + label), cabecera con la letra; empty si no hay apps (`Ninguna app con esta letra.`). Sin reloj/hábitos obligatorios en este modo (pueden ocultarse para dejar sitio al listado, como Niagara).
5. **Búsqueda** — un campo en Home: con **query no vacía**, filtra **todas** las apps instaladas por substring de `label` (manda sobre ★/letra); con query vacía, vuelve el modo scrubber (★ o letra activa).
6. **Back / Home** — Back en modo letra (o con query) vuelve a ★ y limpia query si aplica; no hace `finish()` de la Activity. `onNewIntent` deja modo ★ y query vacía.
7. **Tap / long-press** — tap lanza; long-press abre ficha del SO (009). No muta favoritas desde el listado.
8. **Scrubber UI** — ★ arriba + A–Z + `#`; arrastre/selección. Arco/curva: **debe intentar** (como 004); si no, scrubber recto usable.
9. **Estado** — selección ★/letra y query **efímeros** (no DataStore).
10. **Capas** — Compose sin `LauncherApps` / `PackageManager` / `ContentResolver`.
11. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-013-01 — Scrubber en Home

El sistema debe mostrar en Home un scrubber con ★, letras A–Z y `#` que permita elegir el modo de lista.

### RF-013-02 — Modo favoritas

Cuando el scrubber esté en ★ (y la query de búsqueda esté vacía), el sistema debe mostrar el contenido de Home de favoritas (reloj, evento si hay, búsqueda, favoritas, hábitos, CTA según specs previas).

### RF-013-03 — Modo letra

Cuando el usuario seleccione una letra o `#` (query vacía), el sistema debe listar las `InstalledApp` cuyo `label` pertenece a esa letra (criterio 004), con icono monocromo + label, y permitir lanzarlas.

### RF-013-04 — Vacío por letra

Mientras una letra no tenga apps, el sistema debe mostrar el empty de letra sin inventar filas.

### RF-013-05 — Sin overlay A–Z duplicado

El sistema no debe abrir el overlay de apps de 004 por swipe-up (ni otra superficie equivalente) para el listado A–Z; el scrubber de Home es el escape hatch.

### RF-013-06 — Búsqueda manda

Mientras la query de búsqueda no esté vacía, el sistema debe filtrar todas las apps por substring de `label` e ignorar el filtro ★/letra del scrubber.

### RF-013-07 — Back y vuelta a Home

Cuando el usuario pulse Back fuera de ★ con query vacía, el sistema debe volver a ★. Cuando llegue `onNewIntent`, el sistema debe quedar en ★ con query vacía. En ningún caso debe hacer `finish()` de la Activity HOME.

### RF-013-08 — Sin mutar favoritas desde listado

El sistema no debe añadir ni quitar favoritas desde el modo letra ni desde resultados de búsqueda globales; la alta/baja permanece en Ajustes.

### RF-013-09 — Capas y rendimiento

Los `@Composable` no deben llamar a `LauncherApps` / `PackageManager` / DataStore / `CalendarContract`. El scrubber debe respetar el presupuesto de `docs/domain.md` (~60 fps en arrastre; RSS idle &lt; 80 MB; cold start / `onNewIntent` metas intactas).

## Copy de UI

- Empty letra: `Ninguna app con esta letra.` (reutilizar 004).
- Empty búsqueda global: coherente con 008 (sin inventar filas).
- ★ sin copy extra obligatorio.

## Impacto en rendimiento

Esta spec **toca el proceso Home** de forma central.

- **RAM idle:** una superficie en lugar de Home + overlay montado; no retener dos árboles de lista. Techo RSS idle &lt; 80 MB.
- **Recomposiciones:** reloj sigue aislado en modo ★; modo letra no debe tickear el reloj si el reloj no está compuesto. Scrubber: evitar recomponer toda la lista en cada pixel del arrastre (letra seleccionada estable).
- **Arranque / `onNewIntent`:** primer frame sigue siendo modo ★; listado letra perezoso al seleccionar.
- **APK:** sin deps nuevas.

## Persistencia local

Sin cambio de esquema. Selección scrubber y query no se persisten.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents (salvo que el plan retire código del overlay sin tocar contrato HOME).

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-013-01 | — | — | scrubber ★+A–Z+# en Home |
| RF-013-02 | — | — | ★ = favoritas + reloj/hábitos |
| RF-013-03 | letter filter (reutilizar dominio 004) | — | letra J → apps J |
| RF-013-04 | — | — | letra vacía → empty |
| RF-013-05 | — | — | swipe-up no abre overlay apps |
| RF-013-06 | filter query | — | búsqueda ignora letra |
| RF-013-07 | — | — | Back → ★; Home caliente → ★ |
| RF-013-08 | — | — | listado no cambia favoritas |
| RF-013-09 | — | — | fps / sin PM en Compose |

## Criterio de hecho

- Home unificado ★ / A–Z como Niagara (sin carpetas-grupo).
- Overlay A–Z de 004 retirado como camino de listado.
- Búsqueda, Back/`onNewIntent`, capas y presupuesto OK.
- PR único en `spec/013-niagara-home-scrubber`.
