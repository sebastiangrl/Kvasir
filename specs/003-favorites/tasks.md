# Tareas 003 — Favoritas + DataStore

**Spec:** `specs/003-favorites/spec.md`  
**Plan:** `specs/003-favorites/plan.md`  
**Rama:** `spec/003-favorites`

## Recorte

- [x] **T1 — DataStore + PreferencesRepository + FavoritesResolver** (RF-003-01, RF-003-02, RF-003-09)
  - Dep `datastore-preferences` en catalog/`app`; `PreferencesKeys` + `PreferencesRepository`; `FavoritesResolver` + test JVM; cablear repo en `AppContainer`.
  - Validado: `./gradlew test assembleDebug` OK.
- [x] **T2 — Home solo favoritas** (RF-003-03, RF-003-04, RF-003-06, RF-003-07)
  - `HomeViewModel`/`HomeScreen`: lista de favoritas resueltas + empty; quitar «Apps instaladas»; botón Ajustes (navegación aún stub o callback); sin DataStore/`LauncherApps` en Composables.
  - Validado: `./gradlew test assembleDebug` OK; Home sin catálogo «Apps instaladas»; Ajustes stub hasta T3.
- [x] **T3 — Ajustes alta/baja + KvasirRoot** (RF-003-05, RF-003-08)
  - `KvasirRoot` Home|Settings; `SettingsViewModel`/`SettingsScreen` con Switch; poda de huérfanas al abrir; Back vuelve a Home sin `finish()`; persistencia inmediata.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente (alta/baja, Home refleja, Back no mata Activity).

## Reglas

- Una tarea por sesión de implementación.
- Citar `003` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` |
| T2 | `./gradlew test assembleDebug`; Home sin catálogo «Apps instaladas» |
| T3 | `./gradlew test assembleDebug` + checklist: alta/baja, Home refleja, Back no mata Activity |

## Checklist manual acumulada (cierre 003)

Cubrir RF-003-03…08 en dispositivo/revisión de capas. RF-003-01, 02, 09: unitarias en T1 (`FavoritesResolverTest` + clave documentada).
