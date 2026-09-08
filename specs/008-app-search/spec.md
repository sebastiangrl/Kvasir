# Spec 008 — Búsqueda de apps

**Estado:** aprobada  
**Rama:** `spec/008-app-search`

## Problema

Con muchas apps, el scrubber del overlay solo por letra inicial obliga a recorrer buckets grandes. En Home, una lista larga de favoritas también es lenta de escanear a ojo. Falta filtrar por texto (substring del label) sin abandonar el hiperfocus, sin iconos y sin persistir ruido en DataStore.

## Fuera de alcance

Gestos nuevos, iconos/`Bitmap`, carpetas, rachas/historial de hábitos, widgets, badges, work profile, atajos, i18n, fuzzy/ranking avanzado, búsqueda por package name, mutar favoritas desde resultados del overlay, filtrar hábitos o el catálogo de Ajustes, cambios de Manifest/`<queries>`/permisos.

**Decisiones de producto:**

1. **Dónde** — búsqueda en **Home** (solo favoritas) y en el **overlay** (todas las `InstalledApp`).
2. **Scrubber + query** — la **query de texto manda**: con texto no vacío, el filtro por letra del scrubber no aplica (se ignora / se limpia la letra activa al escribir). Con query vacía, el scrubber A–Z+# de 004 sigue igual.
3. **Matching** — substring case-insensitive según locale del dispositivo, sobre `label` (no package name).
4. **Persistencia** — query **efímera**: se limpia al cerrar overlay / `onNewIntent`; en Home no se persiste en DataStore.
5. **Capas** — filtro en dominio o ViewModel; `@Composable` no llama a `LauncherApps` / `PackageManager` / DataStore.
6. **Deps** — sin librerías nuevas.

## Requisitos (EARS)

### RF-008-01 — Home

El sistema debe ofrecer un campo de búsqueda en Home. Mientras la query tenga texto, debe mostrar solo las favoritas cuyo `label` contenga ese texto (substring, case-insensitive según locale). Si la query está vacía, debe mostrar la lista completa de favoritas como en 003.

### RF-008-02 — Overlay

El sistema debe ofrecer un campo de búsqueda en el overlay. Mientras la query tenga texto, debe filtrar todas las `InstalledApp` en memoria por substring del `label` (mismo criterio que Home). Si no hay coincidencias, debe mostrar el empty sin inventar filas.

### RF-008-03 — Query manda sobre scrubber

Mientras la query del overlay no esté vacía, el sistema no debe condicionar la lista por la letra del scrubber (la query manda).

### RF-008-04 — Query vacía restaura 004

Cuando la query del overlay esté vacía, el sistema debe filtrar por scrubber A–Z+# como en la spec 004.

### RF-008-05 — Limpieza efímera

Cuando el usuario cierre el overlay, lance una app desde el overlay, o llegue `onNewIntent` (vuelta a Home), el sistema debe limpiar la query del overlay (y el estado de scrubber como en 004). La query de Home no debe persistirse en DataStore.

### RF-008-06 — Capas

Los `@Composable` no deben llamar a `LauncherApps`, `PackageManager` ni DataStore; observan ViewModel/estado. El filtrado debe vivir fuera de la capa de lectura directa del SO.

### RF-008-07 — Rendimiento y límites

El sistema no debe añadir dependencias nuevas ni iconos/`Bitmap`. El filtro debe operar sobre labels ya en memoria (O(n) aceptable). No debe degradar el presupuesto de `docs/domain.md` (RSS idle &lt; 80 MB con overlay cerrado; reloj aislado; cold start / `onNewIntent` metas intactas).

## Copy de UI

- Hint / placeholder del campo: `Buscar apps`
- Empty sin resultados: `Ninguna app coincide.`
- Empty por letra (004) permanece cuando la query está vacía: `Ninguna app con esta letra.`

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Filtro puro sobre listas de strings en memoria; sin I/O ni bitmaps.
- Cambios de query invalidan la lista filtrada; el reloj (`HomeClock`) debe seguir aislado del tick de búsqueda.
- Overlay: query efímera; al cerrar no retener estado de búsqueda.
- RSS idle (overlay cerrado) **&lt; 80 MB**; scrubber con query vacía mantiene metas de 004; cold start / `onNewIntent` metas de `docs/domain.md` intactas.

## Persistencia local

Sin cambio de esquema. Queries efímeras en memoria UI/ViewModel.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-008-01 | Filtro favoritas por substring / locale | | Campo en Home filtra |
| RF-008-02 | Filtro instaladas por substring | | Campo en overlay filtra |
| RF-008-03 | Con query, letra no aplica | | Escribir texto ignora scrubber |
| RF-008-04 | Query vacía → bucket letra | | Borrar texto restaura scrubber |
| RF-008-05 | | | Cerrar / Home limpia query overlay |
| RF-008-06 | | | Revisión: Compose sin PM/LauncherApps/DataStore |
| RF-008-07 | | | Sin deps nuevas; sin iconos |

## Criterio de hecho

- Home y overlay filtran por substring del label con el copy acordado.
- Query no vacía en overlay anula scrubber; vacía restaura 004.
- Queries efímeras; sin DataStore nuevo; sin deps; sin iconos.
- Presupuesto de rendimiento de `docs/domain.md` respetado.
