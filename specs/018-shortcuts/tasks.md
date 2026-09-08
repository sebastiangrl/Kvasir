# Tareas 018 — Atajos de apps

**Spec:** `specs/018-shortcuts/spec.md`  
**Plan:** `specs/018-shortcuts/plan.md`  
**Rama:** `spec/018-shortcuts`

## Recorte

- [x] **T1 — Modelo + mapper + AppShortcutsRepository** (RF-018-01, RF-018-04 parcial, RF-018-05, RF-018-06, RF-018-07)
  - `AppShortcut`, tope 5 / labels, `shortcutsFor` + `startShortcut`; tests JVM del mapper.
  - Sin sheet UI aún.
  - Validación: `./gradlew test assembleDebug`

- [x] **T2 — Long-press → sheet o details** (RF-018-02, RF-018-03, RF-018-04, RF-018-05)
  - `HomeViewModel` + sheet Compose en Home (favoritas + catálogo); launch / dismiss / detalles.
  - Validación: `./gradlew test assembleDebug` + checklist

## Reglas

- Una tarea por sesión de implementación.
- Citar `018` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` |
| T2 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 018)

- App sin atajos → long-press abre detalles (009).
- App con atajos → sheet ≤5 + “Detalles de la app”.
- Tap atajo lanza; fallo no tumba Home.
- Favoritas y catálogo A–Z / búsqueda.
- Compose sin LauncherApps; sin pins nuevos en Home; sin deps.
