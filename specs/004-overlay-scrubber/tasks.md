# Tareas 004 — Overlay scrubber A–Z

**Spec:** `specs/004-overlay-scrubber/spec.md`  
**Plan:** `specs/004-overlay-scrubber/plan.md`  
**Rama:** `spec/004-overlay-scrubber`

## Recorte

- [x] **T1 — LetterBucket + tests** (RF-004-04, RF-004-05)
  - `LetterBucket` (A–Z / `#`, filtro por label locale) + `LetterBucketTest`.
  - Validado: `./gradlew test` OK.
- [x] **T2 — OverlayViewModel + AppsOverlay** (RF-004-03, RF-004-06, RF-004-07, RF-004-08)
  - VM (`isOpen`, letra, filtrado, `open`/`close`/`launchAndClose`; sin PreferencesRepository); `AppsOverlay` lista + scrubber; string empty; montaje perezoso aún puede quedar cableado mínimo o stub hasta T3.
  - Validado: `./gradlew test assembleDebug` OK; overlay perezoso cableado; gestos de apertura en T3.
- [x] **T3 — Gestos + Root + onNewIntent** (RF-004-01, RF-004-02)
  - Swipe-up en Home; swipe-down / Back / launch cierran; `KvasirRoot` Back priority overlay > Settings; `MainActivity.onNewIntent` → `close()`.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente (swipe-up abre; Back / swipe-down / lanzar / `onNewIntent` cierran).

## Reglas

- Una tarea por sesión de implementación.
- Citar `004` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist: swipe-up abre; Back / swipe-down / lanzar / `onNewIntent` cierran |

## Checklist manual acumulada (cierre 004)

Cubrir RF-004-01…03, 06…08 en dispositivo/revisión de capas. RF-004-04, 05: unitarias en T1 (`LetterBucketTest`).
