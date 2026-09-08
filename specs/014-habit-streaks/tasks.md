# Tareas 014 — Rachas e historial de hábitos

**Spec:** `specs/014-habit-streaks/spec.md`  
**Plan:** `specs/014-habit-streaks/plan.md`  
**Rama:** `spec/014-habit-streaks`

## Recorte

- [x] **T1 — Dominio historial + racha + JSON** (RF-014-01, RF-014-03, RF-014-06)
  - Modelo historial; encode/decode `habit_history_json`; poda 90 días.
  - `HabitStreak` (racha + últimos 7 días); tests JVM.
  - Validado: `./gradlew test` OK.

- [x] **T2 — PreferencesRepository archive + Flows** (RF-014-02, RF-014-07)
  - Clave DataStore; archive al rolleear; `ensureHabitDayRolled()`; borrar historial al eliminar hábito.
  - Validado: `./gradlew test` OK.

- [x] **T3 — Home + Ajustes UI** (RF-014-04, RF-014-05, RF-014-08)
  - Home: sufijo racha si ≥ 1; `onResume` llama ensure roll.
  - Ajustes: resumen 7 días / racha por hábito.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `014` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 014)

- Marcar hábito varios días (o simular roll) → racha visible en Home.
- Ajustes muestra marcas de los últimos 7 días.
- Eliminar hábito limpia su historial.
- Sin deps nuevas; Compose sin DataStore directo.
