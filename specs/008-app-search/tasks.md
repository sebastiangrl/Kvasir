# Tareas 008 — Búsqueda de apps

**Spec:** `specs/008-app-search/spec.md`  
**Plan:** `specs/008-app-search/plan.md`  
**Rama:** `spec/008-app-search`

## Recorte

- [x] **T1 — AppLabelFilter + tests** (RF-008-01…04 dominio, RF-008-07 parcial)
  - `AppLabelFilter` (`matches` / `filterByQuery` / `filterOverlay`) + `AppLabelFilterTest`.
  - Validado: `./gradlew test` OK.

- [x] **T2 — Búsqueda en Home** (RF-008-01, RF-008-06 Home)
  - `HomeViewModel` query efímera + filtro tras favoritas; `HomeScreen` campo; empty sin coincidencias vs sin favoritas.
  - Strings `search_apps_hint` / `search_apps_empty`.
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T3 — Búsqueda en Overlay** (RF-008-02…07)
  - `OverlayViewModel` query + query manda sobre scrubber; `close`/`open` limpian; `AppsOverlay` campo + empties; Root cableado.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo RF-008-02…05 pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `008` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist overlay/Home query |

## Checklist manual acumulada (cierre 008)

- Home filtra favoritas; empty correcto.
- Overlay filtra todas; query anula scrubber; vacía restaura 004.
- Cerrar / lanzar / `onNewIntent` limpian query overlay.
- Compose sin PM/LauncherApps/DataStore; sin deps ni iconos.
