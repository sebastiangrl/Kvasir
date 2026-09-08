# Tareas 016 — Ajustes organizados

**Spec:** `specs/016-settings-hub/spec.md`  
**Plan:** `specs/016-settings-hub/plan.md`  
**Rama:** `spec/016-settings-hub`

## Recorte

- [x] **T1 — Modelo de sección + helper permisos + navigators** (RF-016-04 parcial, RF-016-05, RF-016-06, RF-016-07)
  - `SettingsSection`; `PermissionsStatusHelper`; intents de ajustes SO (details / notificaciones / alarmas).
  - Tests JVM de builders puros si aplica. Sin UI hub aún.
  - Validación: `./gradlew test`

- [x] **T2 — Hub + secciones UI (contenido existente)** (RF-016-01, RF-016-02, RF-016-03)
  - Índice; Apariencia / Pomodoro / Favoritas / Hábitos; Back hub↔sección↔Home.
  - Validación: `./gradlew test assembleDebug`

- [x] **T3 — Sección Permisos e Inicio + CTAs Activity** (RF-016-04, RF-016-05)
  - Filas estado + Permitir / Elegir / Abrir ajustes; refresh onResume; launchers calendario/notif.
  - Validación: `./gradlew test assembleDebug` + checklist

## Reglas

- Una tarea por sesión de implementación.
- Citar `016` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 016)

- Ajustes abre índice de secciones.
- Entrar a cada sección y Atrás vuelve al hub; Atrás en hub → Home.
- Tema, Pomodoro, favoritas, hábitos funcionan como antes.
- Permisos: estados correctos; CTAs abren diálogo o ajustes SO.
- Compose sin PackageManager/Settings/DataStore directo; sin deps ni claves DataStore nuevas.
