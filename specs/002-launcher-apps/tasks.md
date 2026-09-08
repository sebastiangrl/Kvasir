# Tareas 002 — Listar y lanzar apps

**Spec:** `specs/002-launcher-apps/spec.md`  
**Plan:** `specs/002-launcher-apps/plan.md`  
**Rama:** `spec/002-launcher-apps`

## Recorte

- [x] **T1 — Modelo + mapper + tests + `<queries>`** (RF-002-01, RF-002-03, RF-002-04, RF-002-05)
  - `InstalledApp`, `InstalledAppMapper` (filtrar self, orden locale), `InstalledAppMapperTest`, Manifest `<queries>` MAIN+LAUNCHER.
  - Validado: `./gradlew test assembleDebug` OK; Manifest con `<queries>` MAIN+LAUNCHER; sin `QUERY_ALL_PACKAGES`.
- [x] **T2 — `LauncherAppsRepository` + DI** (RF-002-02, RF-002-06, RF-002-07, RF-002-11)
  - Repo: carga async, `StateFlow`, `Callback` una vez, `launch` con try/catch; cablear en `AppContainer` / `start()` desde Application.
  - Validado: `./gradlew test assembleDebug` OK; `start()` en `KvasirApp`; Application context only.
- [x] **T3 — Home: lista plana + lanzar** (RF-002-08, RF-002-09, RF-002-10)
  - Extender `HomeViewModel`/`HomeScreen`/`MainActivity`/strings; `LazyColumn` de labels; empty solo si `appsLoaded && empty`; reloj aislado; Compose sin `LauncherApps`.
  - Validado: `./gradlew test assembleDebug` OK; `ui/` Composables sin `PackageManager`/`android.content.pm.LauncherApps`. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `002` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug`; Manifest con `<queries>` MAIN+LAUNCHER; sin `QUERY_ALL_PACKAGES` |
| T2 | `./gradlew assembleDebug`; callback en Application; sin Context de Activity en el repo |
| T3 | `./gradlew test assembleDebug` + checklist: lista se llena; tap lanza; instalar/desinstalar actualiza; reloj no bloqueado; `ui/` sin `LauncherApps`/`PackageManager` |

## Checklist manual acumulada (cierre 002)

Cubrir RF-002-02, 03, 06, 07, 08, 09, 10, 11 en dispositivo/revisión. RF-002-01/04/05: unitarias en T1.
