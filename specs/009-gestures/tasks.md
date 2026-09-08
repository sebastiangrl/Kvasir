# Tareas 009 — Gestos Home (panel sistema + info de app)

**Spec:** `specs/009-gestures/spec.md`  
**Plan:** `specs/009-gestures/plan.md`  
**Rama:** `spec/009-gestures`

## Recorte

- [x] **T1 — SystemPanels + AppDetails + Manifest** (RF-009-01 parcial, RF-009-04 parcial, RF-009-06/07 base)
  - `SystemPanels.expandPreferredPanel` (QS → notificaciones → no-op).
  - `AppDetailsNavigator` + `AppDetailsNavigatorTest`.
  - Manifest: `EXPAND_STATUS_BAR`.
  - Validado: `./gradlew test` OK.

- [x] **T2 — Swipe-down Home + cableado panel** (RF-009-01, RF-009-02, RF-009-03)
  - `HomeScreen` `onSwipeDown` → VM → `SystemPanels`; swipe-up overlay intacto.
  - Overlay swipe-down sigue `onClose` (sin panel).
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T3 — Long-press Home + Overlay** (RF-009-04, RF-009-05, RF-009-06)
  - `combinedClickable` en filas; `openAppDetails` en Home/Overlay VM; no mutar favoritas; overlay no cierra al long-press.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `009` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist long-press / favoritas |

## Checklist manual acumulada (cierre 009)

- Swipe-down Home → QS o notificaciones (o no-op seguro).
- Overlay: swipe-down cierra; swipe-up Home abre overlay.
- Long-press abre ficha del SO; favoritas no cambian.
- Compose sin StatusBar/PM/LauncherApps/DataStore; sin deps nuevas.
