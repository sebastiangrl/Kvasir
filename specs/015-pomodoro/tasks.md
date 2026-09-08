# Tareas 015 — Pomodoro

**Spec:** `specs/015-pomodoro/spec.md`  
**Plan:** `specs/015-pomodoro/plan.md`  
**Rama:** `spec/015-pomodoro`

## Recorte

- [x] **T1 — Dominio config/sesión + máquina de fases + JSON** (RF-015-01 parcial, RF-015-02 parcial, RF-015-05 parcial, RF-015-08 parcial, RF-015-09)
  - Modelos, clamp/defaults, `PomodoroPhaseMachine`, encode/decode, format restante.
  - Tests JVM. Sin Manifest ni UI.
  - Validación: `./gradlew test`

- [x] **T2 — DataStore + AlarmManager + notificaciones + Manifest** (RF-015-01, RF-015-02, RF-015-05, RF-015-06 parcial, RF-015-08)
  - Claves/Flows/escrituras; scheduler; notifier; receiver (+ boot); permisos Manifest.
  - Sin bloque Home/Ajustes aún (API lista para T3).
  - Validación: `./gradlew test assembleDebug`

- [x] **T3 — Home + Ajustes + permiso al Iniciar** (RF-015-03, RF-015-04, RF-015-06, RF-015-07)
  - Bloque Home aislado; sección Ajustes; pedir `POST_NOTIFICATIONS` al primer Iniciar; cablear VM/Root/Activity.
  - Validación: `./gradlew test assembleDebug` + checklist

## Reglas

- Una tarea por sesión de implementación.
- Citar `015` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 015)

- Ajustes: cambiar minutos/N → siguiente Iniciar usa valores nuevos.
- Home: iniciar → restante; pausar/reanudar; detener cancela.
- Fin de fase → notificación + siguiente fase (o idle al completar ciclo).
- Denegar notificaciones → timer en Home sin crash.
- Matar proceso / reboot con timer activo → restaura o reprograma (best-effort).
- Reloj tipográfico no “tiembla” con el tick del Pomodoro; sin deps nuevas.
