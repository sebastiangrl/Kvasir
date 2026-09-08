# Spec 015 — Pomodoro

**Estado:** hecha  
**Rama:** `spec/015-pomodoro`

## Problema

En un launcher hiperfocus falta un **temporizador de foco** propio: el usuario quiere definir sesiones (trabajo / descanso), ver el progreso sin abrir otra app y **recibir un aviso cuando termine** la fase. Hoy no hay timer ni notificaciones de producto en Kvasir; ir a Ajustes del teléfono a buscar otra app rompe el flujo.

## Fuera de alcance

- Host de widgets de terceros / `AppWidgetHost` (descartado del backlog).
- App Widget de Kvasir en otros launchers (posible spec futura).
- Reorganizar todo Ajustes en secciones/hub de permisos (**016**).
- Historial/estadísticas de pomodoros pasados, sync, sonidos custom, DND, burbuja flotante, Wear.
- `FOREGROUND_SERVICE` continuo con tick en barra (salvo que en plan se demuestre imprescindible; default = alarma exacta + notificación al fin).
- Cambiar hábitos (005/014), favoritas, scrubber, calendario, tema salvo espacio de UI para el bloque Pomodoro.

**Decisiones de producto:**

1. **Qué** — ciclo Pomodoro: fases **trabajo** y **descanso**, con **N sesiones de trabajo** por ciclo (N configurable). Tras N trabajos, un descanso largo opcional **o** un solo tipo de descanso en v1 (ver decisión 2).
2. **Duraciones (v1)** — tres enteros en minutos, configurables en Ajustes Kvasir: **trabajo**, **descanso corto**, **sesiones de trabajo por ciclo**. Descanso largo = mismo que corto en v1 (sin cuarto campo) para no hinchar UI; se puede ampliar en 016+.
3. **Defaults** — 25 / 5 / 4 (clásico). Rangos razonables (p. ej. trabajo 1–90, descanso 1–30, sesiones 1–12); valores inválidos se clampean al guardar.
4. **Dónde control** — bloque compacto en **Home** (estado + restante + iniciar / pausar / detener). Configuración de minutos y N **solo en Ajustes** Kvasir (sección Pomodoro), no en Ajustes del SO.
5. **Reloj del timer** — la fuente de verdad es un **instante de fin** (`endsAtEpochMillis`) + fase; la UI calcula restante. **No** atar el tick del reloj tipográfico (011) al Pomodoro. Si Home está visible, refresco acotado del restante (p. ej. 1 Hz solo del bloque timer, o al resume).
6. **Fiabilidad** — programar el fin de fase con **alarma exacta** (best-effort en OEM agresivos). Al disparar: notificación + avanzar o cerrar ciclo según reglas. Persistencia del estado activo en DataStore para sobrevivir muerte de proceso.
7. **Cadena de fases** — al terminar trabajo: notificación + pasar a descanso (si quedan o tras cada trabajo) y **arrancar el descanso automáticamente**; al terminar descanso: si quedan sesiones de trabajo, arrancar trabajo; si el ciclo acabó, notificación de ciclo completo y estado idle. El usuario puede **detener** en cualquier momento.
8. **Pausa** — pausar congela el restante (guarda `remainingMillis`); reanudar reprograma la alarma. Sin pausa infinita: opcional tope no obligatorio en v1.
9. **Notificaciones** — canal propio; aviso al **fin de cada fase** y al **fin de ciclo**. Acciones mínimas en la notificación: p. ej. **Detener** (y si cabe **Pausar**). Requiere `POST_NOTIFICATIONS` (API 33+); pedir en runtime **al primer Iniciar** si no está concedido. Si denegado: el timer **sigue en Home**, pero sin aviso push (documentar).
10. **Permisos Manifest** — `POST_NOTIFICATIONS`; alarma exacta (`SCHEDULE_EXACT_ALARM` y/o `USE_EXACT_ALARM` según plan/API mín.); receptor/servicio mínimo para el fin. Sin `INTERNET`.
11. **Capas** — `@Composable` no programa alarmas ni toca `NotificationManager` / DataStore; ViewModel + capa data/alarma.
12. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-015-01 — Persistencia de config

El sistema debe persistir en DataStore Preferences la configuración Pomodoro (minutos de trabajo, minutos de descanso, sesiones de trabajo por ciclo) con defaults 25 / 5 / 4 y migración aditiva compatible con instalaciones previas.

### RF-015-02 — Estado de sesión activa

El sistema debe persistir el estado de una sesión en curso (fase, índice de sesión, `endsAt` o restante si pausada, idle/running/paused) de forma que, tras matar el proceso, pueda restaurarse y reprogramar la alarma si sigue vigente.

### RF-015-03 — Home control

Mientras exista config válida, el sistema debe mostrar en Home un bloque Pomodoro con estado legible (idle / trabajo / descanso / pausa), tiempo restante cuando aplique, y acciones iniciar, pausar/reanudar y detener.

### RF-015-04 — Ajustes config

El sistema debe permitir en Ajustes Kvasir editar trabajo, descanso y número de sesiones por ciclo, sin obligar a abrir Ajustes del sistema para esos valores.

### RF-015-05 — Alarma de fin

Cuando una fase esté en marcha (no pausada), el sistema debe programar una alarma exacta (best-effort) para el instante de fin y, al dispararse, emitir la notificación correspondiente y aplicar la transición de fase acordada.

### RF-015-06 — Notificación y permiso

Cuando el usuario inicie un Pomodoro y `POST_NOTIFICATIONS` no esté concedido (API que lo exija), el sistema debe poder solicitarlo. Si está denegado, el temporizador debe seguir funcionando en UI Home sin crashear, sin inventar avisos falsos.

### RF-015-07 — Capas y rendimiento

Los `@Composable` no deben llamar a DataStore, `AlarmManager` ni `NotificationManager`. El tick del restante no debe forzar recomposición del reloj tipográfico ni degradar el presupuesto Home de `docs/domain.md`.

### RF-015-08 — Detener y ciclo completo

Cuando el usuario detenga, el sistema debe cancelar la alarma, limpiar estado activo a idle y no dejar notificaciones engañosas de fase en curso. Cuando se completen las N sesiones de trabajo del ciclo (y su descanso final según la cadena), el sistema debe notificar fin de ciclo y volver a idle.

### RF-015-09 — Sin deps

El sistema no debe añadir dependencias Gradle por esta feature.

## Copy de UI

- Home (ejemplos): `Pomodoro`, `Trabajo · 24:59`, `Descanso · 4:12`, `Pausa · 10:00`; acciones `Iniciar`, `Pausar`, `Reanudar`, `Detener`.
- Ajustes: sección `Pomodoro`; labels `Trabajo (min)`, `Descanso (min)`, `Sesiones por ciclo`.
- Notificación: título/texto en español, p. ej. `Trabajo terminado`, `Descanso terminado`, `Ciclo Pomodoro completo`.
- Sin empty inventado si nunca se usó: bloque Home en idle con `Iniciar` basta.

## Impacto en rendimiento

Esta spec **toca el proceso Home** (bloque UI + lectura de estado).

- **RAM idle:** estado pequeño en DataStore/memoria; sin Bitmap; sin host de widgets. RSS idle &lt; 80 MB intacto.
- **Recomposiciones:** restante del Pomodoro en estado/composable **aislado**; no mezclar con tick 1 Hz del reloj 011.
- **Arranque / `onNewIntent`:** restaurar estado y reprogramar alarma en background; no bloquear primer frame del reloj. Metas &lt; 400 ms / &lt; 100 ms percibidos.
- **APK:** sin deps nuevas; receptor/alarma ligeros.

## Persistencia local

- Claves aditivas (nombres finales en plan), p. ej.:
  - config: `pomodoro_work_min`, `pomodoro_break_min`, `pomodoro_sessions` (Ints) **o** un JSON único `pomodoro_config_json`;
  - sesión: `pomodoro_session_json` (fase, índice, endsAt / remaining, status).
- Migración: ausencia → defaults + idle.
- Compatibilidad: no tocar hábitos, favoritas, tema, historial.

## Contrato con Android

- `POST_NOTIFICATIONS` (runtime en API 33+).
- Permiso(s) de alarma exacta según minSdk/target (`SCHEDULE_EXACT_ALARM` y/o `USE_EXACT_ALARM`) — detalle en plan.
- `AlarmManager` + `BroadcastReceiver` (o equivalente mínimo) para fin de fase.
- Canal de notificación + acciones Detener (y Pausar si aplica).
- Sin `INTERNET`, sin host de App Widgets, sin FGS por defecto.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-015-01 | encode/defaults/clamp config | — | — |
| RF-015-02 | restore sesión / pausa remaining | — | matar proceso mid-timer |
| RF-015-03 | — | — | Home iniciar/pausar/detener + restante |
| RF-015-04 | — | — | Ajustes cambia minutos/N y aplica al siguiente inicio |
| RF-015-05 | transición de fases pura (dominio) | — | fin de fase → notificación + siguiente |
| RF-015-06 | — | — | denegar notif → timer en Home sin crash |
| RF-015-07 | — | — | sin Alarm/Notify/DataStore en Compose; reloj aislado |
| RF-015-08 | detener limpia estado (dominio) | — | detener cancela aviso engañoso; ciclo N completo |
| RF-015-09 | — | — | diff Gradle sin deps |

## Criterio de hecho

- Config in-app + control en Home + aviso al fin de fase/ciclo.
- Estado sobrevive muerte de proceso (best-effort alarma).
- Sin host de widgets ajenos; sin deps; presupuesto Home intacto.
- PR único en `spec/015-pomodoro`.
