# Tareas 010 — Iconos monocromos

**Spec:** `specs/010-monochrome-icons/spec.md`  
**Plan:** `specs/010-monochrome-icons/plan.md`  
**Rama:** `spec/010-monochrome-icons`

## Recorte

- [x] **T1 — AppIconLoader + MonochromeAppIcon** (RF-010-03…07 base)
  - `AppIconLoader` (LauncherApps + LRU ≤ 48); `MonochromeAppIcon` (tint SrcIn / onSurface, 28 dp).
  - `AppContainer` + `LocalAppIconLoader` en MainActivity.
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T2 — Iconos en Home** (RF-010-01, RF-010-04, RF-010-05)
  - Filas de favoritas: icono + label; tinte según tema.
  - Validado: `./gradlew test assembleDebug` OK.

- [x] **T3 — Iconos en Overlay** (RF-010-02, RF-010-06)
  - Filas del overlay: icono + label; overlay cerrado no retiene filas Compose.
  - Validado: `./gradlew test assembleDebug` OK. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `010` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist iconos |

## Checklist manual acumulada (cierre 010)

- Favoritas y overlay muestran iconos reales tintados.
- Tema claro/oscuro cambia el tinte.
- Compose sin LauncherApps/PM en UI de iconos; sin deps nuevas; sin cache Bitmap ilimitada.
