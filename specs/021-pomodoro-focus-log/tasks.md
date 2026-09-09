# Tareas 021 — Pomodoro: registro diario, bloque visible y pantalla de bloqueo

**Spec:** `specs/021-pomodoro-focus-log/spec.md`  
**Plan:** `specs/021-pomodoro-focus-log/plan.md`  
**Rama:** `spec/021-pomodoro-focus-log`

## Recorte

- [x] **T1 — Dominio + DataStore historial 30 días** (RF-021-02, RF-021-03 parcial, RF-021-10)
  - `PomodoroDailyLog` + JSON; clave `pomodoro_daily_json`; tests JVM increment/prune.
  - Validación: `./gradlew test assembleDebug`

- [x] **T2 — Incremento al completar trabajo + Hoy en Home** (RF-021-01, RF-021-03)
  - `PomodoroController` incrementa en `WorkFinished`; Home VM + línea `Hoy · N` (UI mínima).
  - Validación: `./gradlew test assembleDebug` + checklist completar 1 trabajo

- [x] **T3 — Ajustes: ayuda ciclo + lista historial** (RF-021-04, RF-021-05)
  - Copy bajo «Sesiones por ciclo»; filas fecha → conteo.
  - Validación: `./gradlew test assembleDebug` + checklist Ajustes

- [x] **T4 — Bloque Home más visible** (RF-021-06, RF-021-09 parcial)
  - Contenedor Surface; restante tipográficamente mayor; tick aislado.
  - Validación: `./gradlew test assembleDebug` + checklist Home

- [x] **T5 — Notificaciones: trabajo→descanso + ongoing bloqueo** (RF-021-07, RF-021-08, RF-021-09)
  - Copy fin trabajo; ongoing chronometer + pause/resume/stop; dos IDs; vibración en avisos de fase.
  - Validación: `./gradlew test assembleDebug` + checklist sombra/bloqueo

## Reglas

- Una tarea por sesión de implementación.
- Citar `021` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` |
| T2 | `./gradlew test assembleDebug` + checklist Hoy · N |
| T3 | `./gradlew test assembleDebug` + checklist historial/ayuda |
| T4 | `./gradlew test assembleDebug` + checklist bloque |
| T5 | `./gradlew test assembleDebug` + checklist ongoing / copy |

## Checklist manual acumulada (cierre 021)

- Completar trabajo → Hoy · N++; Detener a mitad no suma.
- Historial 30 días en Ajustes; ayuda de ciclo visible.
- Bloque Home más visible.
- Notif «empieza el descanso» (con vibración); ongoing en bloqueo; desaparece al Detener.
