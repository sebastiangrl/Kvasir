# Tareas 011 — Reloj tipográfico

**Spec:** `specs/011-typographic-clock/spec.md`  
**Plan:** `specs/011-typographic-clock/plan.md`  
**Rama:** `spec/011-typographic-clock`

## Recorte

- [x] **T1 — ClockFormat tipográfico + tests** (RF-011-01, RF-011-02, RF-011-03)
  - `formatWeekdayAbbreviated` (`EEE` + mayúsculas por locale).
  - Fecha corta localizada (día+mes; no `FormatStyle.FULL`).
  - Conservar `formatTime` por locale/sistema.
  - Actualizar `ClockFormatTest` (ES + US).
  - Validado: `./gradlew test` OK.

- [x] **T2 — HomeClock layout Niagara** (RF-011-01, RF-011-02, RF-011-04, RF-011-05, RF-011-06)
  - `Row`: día dominante + `Column` hora/fecha; tipografía Material (sin deps/fuentes nuevas).
  - Tick 1 Hz aislado; sin UI de calendario.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `011` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 011)

- Día abreviado grande + hora + fecha legibles en Home.
- Locale del dispositivo se refleja en los tres campos.
- Favoritas/hábitos no parpadean con el tick del reloj.
- Sin fila de evento de calendario; sin deps tipográficas nuevas.
