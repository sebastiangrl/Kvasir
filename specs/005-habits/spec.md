# Spec 005 — Hábitos del día

**Estado:** aprobada  
**Rama:** `spec/005-habits`

## Problema

Home ya tiene favoritas y overlay, pero falta la sección de **hábitos del día** del dominio: ítems diarios con check, editables en Ajustes, persistidos, y con reset automático al cambiar el día calendario.

## Fuera de alcance

Tema claro/oscuro (006), rachas/historial, notificaciones, reordenar con drag, iconos, work profile. No cambiar contrato HOME, `<queries>` ni permisos. No añadir dependencias nuevas. Favoritas (003) y overlay (004) intactos en comportamiento salvo el espacio de UI en Home/Ajustes para hábitos.

**Decisiones de producto:**

1. **Modelo** — `Habit` = `id` estable + `label`; `HabitDayState` = `epochDay` + set de ids cumplidos hoy.
2. **Persistencia** — DataStore Preferences; JSON con `org.json` del SDK (sin kotlinx.serialization). Claves nuevas aditivas: `habits_json`, `habit_day_state_json`. Favoritas 003 no se migran ni se tocan.
3. **Home** — sección de hábitos (texto + control check/toggle); marcar/desmarcar persiste de inmediato. Reloj aislado a 1 Hz (no invalida la lista de hábitos).
4. **Cambio de día** — al leer o escribir el day state, si `epochDay` ≠ hoy → reset (cumplidos vacíos, `epochDay` = hoy).
5. **Ajustes** — alta, baja y renombrar hábitos en la pantalla/sección de Ajustes existente.
6. **Vacío** — sin hábitos: mostrar `Sin hábitos todavía.` sin inventar filas.
7. **Capas** — `@Composable` no llama a DataStore; observa ViewModel/repos.

## Requisitos (EARS)

### RF-005-01 — Esquema

El sistema debe persistir la lista de hábitos y el estado del día en DataStore Preferences bajo las claves `habits_json` y `habit_day_state_json` (JSON vía `org.json`). Default si faltan: lista vacía y day state vacío/hoy.

### RF-005-02 — Home

El sistema debe mostrar en Home la lista de hábitos con su estado cumplido/no cumplido para el día actual.

### RF-005-03 — Toggle

Cuando el usuario marque o desmarque un hábito en Home, el sistema debe persistir el cambio de inmediato en `HabitDayState`.

### RF-005-04 — Reset diario

Cuando el sistema lea o escriba el day state y `epochDay` no sea el día calendario actual, debe resetear los cumplidos (set vacío) y actualizar `epochDay` a hoy antes de continuar.

### RF-005-05 — Ajustes

El sistema debe permitir en Ajustes crear, eliminar y renombrar hábitos de forma persistente.

### RF-005-06 — Vacío

Mientras no haya hábitos, el sistema debe mostrar `Sin hábitos todavía.` sin inventar filas.

### RF-005-07 — Capas y reloj

Los `@Composable` no deben llamar a DataStore; el tick del reloj a 1 Hz no debe forzar la recomposición de la lista de hábitos.

### RF-005-08 — Compatibilidad

Instalaciones previas (001–004) sin claves de hábitos arrancan con lista vacía y day state por defecto. Cambios futuros de esquema exigirán migración declarada en su spec.

## Copy de UI

- Empty hábitos: `Sin hábitos todavía.`
- Cabecera sección Home (si aplica): `Hábitos`
- En Ajustes: acciones claras para añadir / eliminar / renombrar (p. ej. `Añadir hábito`, `Eliminar`, campo de nombre).

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Lectura DataStore en frío vía Flow; no I/O en cada frame del reloj.
- Lista corta de hábitos; reloj aislado (`HomeClock`).
- Sin iconos/`Bitmap`. RSS idle **&lt; 80 MB**; cold start / `onNewIntent` metas de `docs/domain.md` intactas.
- Sin dependencias nuevas (JSON del SDK).

## Persistencia local

- `habits_json`: array JSON de `{ "id", "label" }`.
- `habit_day_state_json`: objeto JSON `{ "epochDay", "completedIds": [...] }`.
- Migración: N/A (claves nuevas aditivas). Default = vacío.
- Compatibilidad: apps 003/004 sin estas claves → hábitos vacíos; `favorite_component_keys` intacta.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents.

## Dependencias

Sin dependencias nuevas. Usar `org.json` del SDK Android/JDK según el plan de implementación.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-005-01 | Parse/serialización JSON / defaults | | |
| RF-005-02 | | | Home muestra hábitos + checks |
| RF-005-03 | | | Toggle persiste tras matar proceso |
| RF-005-04 | Reset si epochDay ≠ hoy | | Cambio de día (o fake clock en test) |
| RF-005-05 | | | Ajustes: alta/baja/renombrar |
| RF-005-06 | | | Empty sin inventar filas |
| RF-005-07 | | | Revisión: Compose sin DataStore; reloj aislado |
| RF-005-08 | Default vacío | | Instalar sobre 004 → hábitos vacíos |

## Criterio de hecho

- Hábitos y checks del día persisten tras matar el proceso.
- Home muestra hábitos con toggle; empty si no hay.
- Ajustes permite alta/baja/renombrar.
- Cambio de día resetea checks.
- Sin deps nuevas; presupuesto de `docs/domain.md` respetado.
