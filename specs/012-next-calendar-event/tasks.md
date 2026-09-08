# Tareas 012 — Próximo evento de calendario

**Spec:** `specs/012-next-calendar-event/spec.md`  
**Plan:** `specs/012-next-calendar-event/plan.md`  
**Rama:** `spec/012-next-calendar-event`

## Recorte

- [x] **T1 — Modelo + repo + format + Manifest** (RF-012-01, RF-012-03 format, RF-012-05 base, RF-012-08)
  - `NextCalendarEvent`; `CalendarEventsRepository` (`Instances`, horizonte 14d, null best-effort).
  - `NextEventLineFormat` + tests JVM.
  - Manifest: solo `READ_CALENDAR` (sin `WRITE_CALENDAR`).
  - `AppContainer` expone el repo.
  - Validado: `./gradlew test` OK.

- [x] **T2 — ViewModel + permiso + refresh** (RF-012-02, RF-012-04, RF-012-06)
  - `HomeViewModel`: `nextEvent` StateFlow; refresh en `onResume` solo si concedido.
  - `MainActivity`: `RequestPermission` una vez por proceso; luego refresh.
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T3 — UI Home + tap** (RF-012-03, RF-012-05, RF-012-07)
  - Línea bajo el reloj solo si hay evento; clickable → `CalendarEventNavigator`.
  - Sin empty inventado; Compose sin `ContentResolver`.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `012` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 012)

- Conceder `READ_CALENDAR` → aparece próximo evento bajo el reloj (si hay uno).
- Denegar / revocar → sin fila; no re-pide en la misma sesión.
- Sin eventos en 14 días → sin fila.
- Tap abre calendario/evento; tick del reloj no dispara query.
- Manifest sin `WRITE_CALENDAR`; sin deps nuevas.
