# Spec 003 — Favoritas + DataStore

**Estado:** aprobada  
**Rama:** `spec/003-favorites`

## Problema

Con 002 se pueden listar y lanzar todas las apps, pero el Home hiperfocus exige un subconjunto **persistido** (favoritas). Sin DataStore y sin superficie de alta/baja, el empty de 001 nunca se convierte en el producto.

## Fuera de alcance

Overlay/scrubber (004), hábitos, tema persistido, iconos, carpetas, reordenar favoritas con drag, work profile, Room. No cambiar contrato HOME ni `<queries>` salvo lo ya en 002.

**Decisiones de producto:** Home muestra **solo favoritas** (texto, tap = lanzar). La lista plana «Apps instaladas» de 002 **deja de vivir en Home** y pasa a Ajustes como catálogo para marcar favoritas. Favorita huérfana (app desinstalada): no se muestra en Home.

## Requisitos (EARS)

### RF-003-01 — Esquema

El sistema debe persistir favoritas en DataStore Preferences bajo la clave `favorite_component_keys` (`Set` de `componentKey`). Primera clave de prefs: sin migración de datos previos.

### RF-003-02 — Resolución

El sistema debe mostrar en Home solo favoritas cuyo `componentKey` exista aún en la lista de `InstalledApp`; orden alfabético por `label` (locale).

### RF-003-03 — Lanzar

Cuando el usuario pulse una favorita, el sistema debe lanzarla igual que en 002 (`LauncherApps` vía repositorio/ViewModel).

### RF-003-04 — Vacío

Mientras no haya favoritas resolubles, el sistema debe mostrar el empty `Sin favoritas todavía.` (sin inventar filas).

### RF-003-05 — Ajustes

El sistema debe ofrecer una pantalla/sección de Ajustes alcanzable desde Home que liste las apps instaladas (texto) y permita añadir/quitar favorita de forma persistente.

### RF-003-06 — Home limpio

El sistema no debe mostrar en Home la lista provisional «Apps instaladas» de 002; el catálogo completo vive en Ajustes (hasta que 004 lo mueva al overlay).

### RF-003-07 — Capas

Los `@Composable` no deben llamar a DataStore ni `LauncherApps` directamente; observan ViewModel/repos.

### RF-003-08 — Escritura

Cuando el usuario añada o quite una favorita, el sistema debe persistir de inmediato y reflejar el cambio en Home al volver.

### RF-003-09 — Compatibilidad

Instalaciones previas sin DataStore arrancan con set vacío (comportamiento por defecto). Cambios futuros de esquema exigirán migración declarada en su spec.

## Copy de UI

- Empty Home: `Sin favoritas todavía.` (ya existe)
- Entrada a ajustes: `Ajustes`
- Cabecera catálogo en ajustes: `Elegir favoritas`
- Estado favorita (sin iconos): control claro para marcar/desmarcar (p. ej. toggle o texto `Favorita`)

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- DataStore: lectura en frío una vez; Flow; no I/O en cada frame del reloj.
- Home lista corta (favoritas); reloj aislado a 1 Hz.
- Sin iconos/`Bitmap`. RSS idle **< 80 MB**; cold start / `onNewIntent` metas de `docs/domain.md` intactas.
- Dep nueva: DataStore Preferences — justificada (persistencia acordada; más ligera que Room).

## Persistencia local

- Clave: `favorite_component_keys` (`stringSetPreferencesKey`).
- Valores: `componentKey` (`package/class`).
- Migración: N/A (primer esquema). Default = set vacío.
- Compatibilidad: apps 001/002 sin prefs → favoritas vacías.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents respecto a 002.

## Dependencias

Añadir **DataStore Preferences** (`androidx.datastore:datastore-preferences`): peso bajo vs beneficio (única persistencia v1 acordada). **No** Room ni kotlinx.serialization.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-003-01 | Esquema/clave / set vacío default | | |
| RF-003-02 | Resolución favoritas ∩ instaladas + orden | | |
| RF-003-03 | | | Tap favorita lanza |
| RF-003-04 | | | Empty sin inventar filas |
| RF-003-05 | | | Ajustes: alta/baja persistente |
| RF-003-06 | | | Home sin lista «Apps instaladas» |
| RF-003-07 | | | Revisión: Compose sin DataStore/`LauncherApps` |
| RF-003-08 | | | Cambio en Ajustes visible al volver a Home |
| RF-003-09 | Default set vacío | | Instalar sobre 002 → favoritas vacías |

## Criterio de hecho

- Favoritas persisten tras matar el proceso.
- Home muestra solo favoritas (texto); empty si no hay resolubles.
- Ajustes permite alta/baja contra el catálogo instalado.
- Lista provisional de 002 fuera de Home.
- DataStore Preferences añadido y justificado.
- Presupuesto de rendimiento de `docs/domain.md` respetado.
