# Spec 016 — Ajustes organizados

**Estado:** hecha  
**Rama:** `spec/016-settings-hub`

## Problema

Ajustes de Kvasir es una **lista plana** (tema, Pomodoro, favoritas, hábitos) que crece y mezcla configuración de producto con cosas que el SO aún exige (permisos, app de Inicio). El usuario quiere **todo organizado desde la app**, sin ir a ciegas a Ajustes del teléfono, entendiendo el estado de calendario, notificaciones e Inicio.

## Fuera de alcance

- Cambiar RF de favoritas, hábitos, rachas, Pomodoro, tema o Home (solo reorganizar UI de Ajustes / hub).
- Badges, work profile, atajos, per-app theming (017+).
- Pedir permisos en bucle en Home (012/015 siguen; el hub **complementa** con estado + CTA explícitos).
- Sustituir diálogos runtime del SO (siguen siendo del sistema).
- Redesign visual completo / Material Navigation Drawer.
- Dependencias nuevas.

**Decisiones de producto:**

1. **Hub** — Ajustes abre primero un **índice de secciones** (filas con título + subtítulo corto). Tap abre la sección. Atrás desde sección → hub; Atrás desde hub → Home (como hoy).
2. **Secciones** — Apariencia (tema); Pomodoro (minutos/sesiones); Favoritas (catálogo); Hábitos (CRUD + rachas/7 días); **Permisos e Inicio** (hub de permisos).
3. **Permisos e Inicio** — filas con estado legible (p. ej. Concedido / Denegado / Desconocido) y acción:
   - **App de Inicio** — si Kvasir no es default, CTA que abre el picker/ajustes de Home ya existentes (001).
   - **Calendario** (`READ_CALENDAR`) — si no concedido: pedir runtime **o** abrir ficha de la app / ajustes de permisos del SO (best-effort); si concedido: solo estado.
   - **Notificaciones** (`POST_NOTIFICATIONS`, API 33+) — igual; en API &lt; 33 mostrar concedido/N/A según plataforma.
   - **Alarmas exactas** — si el SO lo permite consultar: estado + enlace a ajustes de alarmas de la app cuando aplique; si no es consultable, fila informativa breve o omitir.
4. **Capas** — `@Composable` no llama a `PackageManager` / `Settings` / DataStore; estado y intents vía ViewModel / helpers (Application context).
5. **Home** — sin cambios de layout en Home; solo navegación interna de Ajustes.
6. **Persistencia** — no guardar sección abierta (efímero). Sin claves DataStore nuevas.
7. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-016-01 — Índice de secciones

Cuando el usuario abra Ajustes, el sistema debe mostrar un índice de secciones (Apariencia, Pomodoro, Favoritas, Hábitos, Permisos e Inicio) en lugar de una sola lista mezclada de todos los controles.

### RF-016-02 — Navegación

Cuando el usuario elija una sección, el sistema debe mostrar el contenido de esa sección. Cuando pulse Atrás en una sección, debe volver al índice. Cuando pulse Atrás en el índice, debe volver a Home.

### RF-016-03 — Contenido existente

El sistema debe exponer en las secciones correspondientes la misma funcionalidad ya existente de tema, Pomodoro, favoritas y hábitos (incl. rachas/7 días), sin regresiones de RF previos.

### RF-016-04 — Hub de permisos

El sistema debe mostrar en la sección Permisos e Inicio el estado de app de Inicio, calendario y notificaciones (y alarmas exactas si aplica), con CTA para conceder o abrir el ajuste del SO cuando el estado no sea óptimo.

### RF-016-05 — Capas

Los `@Composable` no deben consultar permisos ni lanzar intents de ajustes del sistema directamente; observan estado del ViewModel y delegan acciones.

### RF-016-06 — Sin esquema nuevo

El sistema no debe añadir claves DataStore por esta spec.

### RF-016-07 — Sin deps

El sistema no debe añadir dependencias Gradle por esta feature.

## Copy de UI

- Índice: `Ajustes`; filas p. ej. `Apariencia`, `Pomodoro`, `Favoritas`, `Hábitos`, `Permisos e Inicio` con subtítulos cortos (p. ej. `Tema claro u oscuro`, `Calendario, notificaciones, app de Inicio`).
- Permisos: `App de Inicio`, `Calendario`, `Notificaciones`, `Alarmas exactas` (si hay fila); estados `Concedido`, `Denegado`, `No es la app de Inicio`, `Es la app de Inicio`; CTAs `Elegir`, `Permitir`, `Abrir ajustes`.
- Español en toda la UI de esta spec.

## Impacto en rendimiento

Esta spec **no cambia el layout de Home**. Misma Activity; Ajustes solo se compone al navegar allí.

- RAM: estado de permisos es un puñado de booleans; sin Bitmap.
- Recomposiciones Home: sin impacto adicional en reloj / scrubber.
- Arranque / `onNewIntent`: intactos.
- RSS idle &lt; 80 MB intacto.

## Persistencia local

Sin cambio de esquema DataStore.

## Contrato con Android

- Sin permisos Manifest **nuevos** (reutilizar los ya declarados: calendario, notificaciones, etc.).
- Intents best-effort hacia ajustes del SO: Home picker / `ACTION_HOME_SETTINGS`, detalles de la app, ajustes de notificaciones de la app, ajustes de alarmas exactas si aplica.
- Runtime permission requests desde Activity/ViewModel (mismo patrón 012/015), disparados por CTA del hub cuando corresponda.
- Sin `INTERNET`.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-016-01 | — | — | Ajustes abre índice |
| RF-016-02 | — | — | sección → Atrás → hub → Atrás → Home |
| RF-016-03 | — | — | tema / Pomodoro / favoritas / hábitos intactos |
| RF-016-04 | helper estado permisos (puro si hay) | — | CTAs abren diálogo o ajustes SO |
| RF-016-05 | — | — | sin PM/Settings en Compose |
| RF-016-06 | — | — | sin claves DataStore nuevas |
| RF-016-07 | — | — | diff Gradle |

## Criterio de hecho

- Ajustes navegable por secciones; hub de permisos con estado + CTA.
- Sin regresiones de config existente; sin esquema DataStore ni deps nuevas.
- PR único en `spec/016-settings-hub`.
