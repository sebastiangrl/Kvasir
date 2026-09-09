# Tareas 020 — Pulido Home/Ajustes + acceso a notificaciones

**Spec:** `specs/020-settings-home-polish/spec.md`  
**Plan:** `specs/020-settings-home-polish/plan.md`  
**Rama:** `spec/020-settings-home-polish`

## Recorte

- [x] **T1 — Reloj centrado + fuente del día + icono Ajustes** (RF-020-01, RF-020-02, RF-020-03)
  - `HomeClock` eje centrado; font display; IconButton engranaje.
  - Validación: `./gradlew test assembleDebug` + checklist

- [x] **T2 — Siluetas nítidas** (RF-020-04)
  - Raster 2–3× + máscara luma/alfa; tests JVM de ops.
  - Validación: `./gradlew test assembleDebug` + checklist apps

- [x] **T3 — Scrubber continuo + háptica** (RF-020-05)
  - Burbuja interpolada; click háptico al cambiar letra.
  - Validación: `./gradlew test assembleDebug` + checklist

- [x] **T4 — Ajustes: atrás, Pomodoro Guardar, Favoritas buscar, Hábitos iconos** (RF-020-06…09)
  - SettingsScreen + strings.
  - Validación: `./gradlew test assembleDebug` + checklist

- [x] **Bugfix — Buscador Home no pierde foco al primer carácter** (RF-008 continuidad)
  - `OutlinedTextField` fuera del switch ★/catálogo; reloj solo en chrome.
  - Validación: `./gradlew test assembleDebug` + checklist teclado

- [x] **T5 — Copy Restricted settings + CTAs** (RF-020-10)
  - Hub permisos; ficha de app + listener.
  - Validación: `./gradlew test assembleDebug` + checklist sideload

## Reglas

- Una tarea por sesión de implementación.
- Citar `020` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` + checklist reloj/icono |
| T2 | `./gradlew test assembleDebug` + checklist siluetas |
| T3 | `./gradlew test assembleDebug` + checklist arco/háptica |
| T4 | `./gradlew test assembleDebug` + checklist Ajustes |
| T5 | `./gradlew test assembleDebug` + checklist restricted |

## Checklist manual acumulada (cierre 020)

- Día display; bloque centrado; engranaje → Ajustes.
- Iconos nítidos (no mancha).
- Burbuja se desliza; vibración al cambiar letra.
- Flecha atrás; Guardar Pomodoro; buscar Favoritas; hábitos con + / eliminar.
- Copy Restricted settings; se puede abrir ficha y listener.
