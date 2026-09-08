# Tareas 001 — Esqueleto Home

**Spec:** `specs/001-home-skeleton/spec.md`  
**Plan:** `specs/001-home-skeleton/plan.md`  
**Rama:** `spec/001-home-skeleton`

## Recorte

- [x] **T1 — Scaffold Gradle + identidad** (RF-001-01, RF-001-02, RF-001-11)
  - Version Catalog, wrapper, módulo `:app`, `applicationId` `app.kvasir.launcher`, `minSdk 26` / `targetSdk`/`compileSdk` 36, deps solo las de la spec, label **Kvasir**, sin DataStore/Hilt/`INTERNET`/`queries`.
  - Validado: `./gradlew assembleDebug test` (BUILD SUCCESSFUL, 2026-09-08).
- [x] **T2 — Contrato HOME + DI + Activity** (RF-001-03, RF-001-04, RF-001-05, RF-001-10)
  - Manifest HOME + flags; `KvasirApp` + `AppContainer`; `MainActivity` (`ComponentActivity`, `onNewIntent`/`setIntent`, back que no hace `finish`, `enableOnBackInvokedCallback`); `setContent` mínimo con tema; sin `PackageManager` en UI.
  - Validado: `./gradlew assembleDebug` OK; Manifest fusionado con MAIN/HOME/DEFAULT, `singleTask`, `clearTaskOnLaunch`, `stateNotNeeded`, `resumeWhilePausing` (sustituye `resumeOnTaskLaunch`, atributo inexistente en AAPT), `enableOnBackInvokedCallback`. Checklist en dispositivo pendiente (picker / Back / Home caliente).
- [x] **T3 — Home UI: reloj aislado + empty** (RF-001-06, RF-001-07)
  - `HomeClock` 1 Hz aislado; formateador puro + `ClockFormatTest`; `HomeScreen` con empty `Sin favoritas todavía.`; sin lista falsa de apps.
  - Validado: `./gradlew test assembleDebug` OK (`ClockFormatTest`). Checklist visual en dispositivo pendiente (tick 1 Hz sin parpadeo del empty).
- [x] **T4 — CTA «No soy Inicio» + releer en resume** (RF-001-08, RF-001-09)
  - `DefaultHomeRepository` (`resolveActivity` + RoleManager / `ACTION_HOME_SETTINGS`); `HomeViewModel` + factory; CTA condicional; releer en `ON_RESUME`; copy ES de la spec.
  - Validado: `./gradlew test assembleDebug` OK; `PackageManager`/`RoleManager` solo en `data/home`. Checklist en dispositivo pendiente (CTA → selector → volver oculta CTA).

## Reglas

- Una tarea por sesión de implementación.
- Citar `001` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew assembleDebug test` (mínimo `assembleDebug`) |
| T2 | Checklist dispositivo: sale en picker de Inicio; Back no cierra; Home caliente vía `onNewIntent`; revisión Manifest |
| T3 | `./gradlew test` + checklist: segundos cambian; empty visible; resto de Home no parpadea con el tick |
| T4 | Checklist: CTA si no somos default; abre selector/ajustes; tras elegir Kvasir y volver, CTA desaparece; Compose no importa `PackageManager` |

## Checklist manual acumulada (cierre 001)

Cubrir RF-001-01…05, 07–11 según la tabla de la spec. RF-001-06: unitaria en T3 + aislamiento visual.
