# Tareas 013 — Home + scrubber unificado (Niagara)

**Spec:** `specs/013-niagara-home-scrubber/spec.md`  
**Plan:** `specs/013-niagara-home-scrubber/plan.md`  
**Rama:** `spec/013-niagara-home-scrubber`

## Recorte

- [x] **T1 — Dominio + HomeViewModel (modos ★ / letra / query)** (RF-013-02, RF-013-03, RF-013-06, RF-013-07 base)
  - `HomeListMode` (★ | letra); estado en `HomeViewModel`.
  - Query no vacía → filtro global de apps; vacía → ★ o letra (`AppLabelFilter` / `LetterBucket`).
  - `resetToFavorites()`; tests JVM del filtro/modos.
  - Validado: `./gradlew test` OK.

- [x] **T2 — HomeScreen + scrubber rail** (RF-013-01, RF-013-02, RF-013-03, RF-013-04)
  - `HomeScrubberRail` (★ + A–Z + `#`); cuerpos ★ vs letra vs búsqueda.
  - Iconos monocromos en listados; empty por letra.
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T3 — Retirar overlay + Back / onNewIntent** (RF-013-05, RF-013-07, RF-013-08, RF-013-09)
  - Quitar `AppsOverlay` / `OverlayViewModel` del Root y MainActivity; swipe-up no abre apps.
  - Back y `onNewIntent` → ★ + query vacía; sin mutar favoritas desde listado.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `013` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 013)

- Scrubber ★ + A–Z + `#` visible en Home.
- ★ = reloj / evento / favoritas / hábitos; letra = listado de esa letra.
- Búsqueda global ignora letra; Back y Home caliente vuelven a ★.
- Swipe-up no abre overlay de apps; sin mutar favoritas desde el listado.
