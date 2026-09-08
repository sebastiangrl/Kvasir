# Tareas 019 — Pulido visual de Home

**Spec:** `specs/019-home-visual-polish/spec.md`  
**Plan:** `specs/019-home-visual-polish/plan.md`  
**Rama:** `spec/019-home-visual-polish`

## Recorte

- [x] **T1 — Edge-to-edge + degradado** (RF-019-01, RF-019-05)
  - Tema ventana transparente; `enableEdgeToEdge`; fondo degradado en Root; insets en Home/Ajustes.
  - Validación: `./gradlew test assembleDebug` + checklist barra/fondo

- [x] **T2 — Silueta de iconos** (RF-019-02)
  - Foreground adaptativo + tint; sin disco opaco.
  - Validación: `./gradlew test assembleDebug` + checklist apps reales

- [x] **T3 — Buscador y checkboxes redondos** (RF-019-03)
  - Shapes en `HomeScreen` (y Ajustes hábitos si el mismo checkbox).
  - Validación: `./gradlew test assembleDebug` + checklist

- [x] **T4 — Reloj + evento (composición)** (RF-019-06)
  - `HomeClock` día centrado; hora SHORT izq; fecha der; evento debajo; tests JVM.
  - Validación: `./gradlew test assembleDebug` + checklist

- [x] **T5 — Scrubber compacto + arco** (RF-019-04)
  - Rail denso; arco y burbuja en drag.
  - Validación: `./gradlew test assembleDebug` + checklist

## Reglas

- Una tarea por sesión de implementación.
- Citar `019` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` + checklist status/nav + degradado |
| T2 | `./gradlew test assembleDebug` + checklist siluetas |
| T3 | `./gradlew test assembleDebug` + checklist formas |
| T4 | `./gradlew test assembleDebug` + checklist reloj |
| T5 | `./gradlew test assembleDebug` + checklist arco |

## Checklist manual acumulada (cierre 019)

- Status/nav del mismo fondo que Home (sin franja gris).
- Iconos = glifo monocromo, no círculo blanco.
- Buscar apps + checkboxes redondeados.
- Scrubber compacto; al arrastrar, arco + burbuja.
- Degradado en claro y oscuro.
- TUE (o MAR) centrado; hora sin segundos a la izquierda; fecha a la derecha; evento debajo.
- Compose sin LauncherApps; sin deps nuevas.
