# Tareas 005 — Hábitos del día

**Spec:** `specs/005-habits/spec.md`  
**Plan:** `specs/005-habits/plan.md`  
**Rama:** `spec/005-habits`

## Recorte

- [x] **T1 — Modelo + HabitJson + HabitDayRoll + prefs keys/repo** (RF-005-01, RF-005-04, RF-005-08)
  - `Habit` / `HabitDayState`; codec JSON + roll; keys + Flows/CRUD en `PreferencesRepository`; tests JVM.
  - Validado: `./gradlew test` OK. (`testImplementation org.json` para stubs JVM).
- [x] **T2 — Home hábitos + toggle** (RF-005-02, RF-005-03, RF-005-06, RF-005-07)
  - `HomeViewModel`/`HomeScreen`: sección hábitos + Checkbox + empty; reloj aislado; Compose sin DataStore.
  - Validado: `./gradlew test assembleDebug` OK.
- [x] **T3 — Ajustes CRUD hábitos** (RF-005-05)
  - Sección en `SettingsScreen`/`SettingsViewModel`: añadir / eliminar / renombrar; strings ES.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente (alta/baja/renombrar; Home refleja).

## Reglas

- Una tarea por sesión de implementación.
- Citar `005` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist: alta/baja/renombrar; Home refleja |

## Checklist manual acumulada (cierre 005)

Cubrir RF-005-02, 03, 05…07 en dispositivo/revisión. RF-005-01, 04, 08: unitarias en T1.
