# Tareas 006 — Tema claro/oscuro

**Spec:** `specs/006-theme/spec.md`  
**Plan:** `specs/006-theme/plan.md`  
**Rama:** `spec/006-theme`

## Recorte

- [x] **T1 — ThemeMode + prefs keys/repo + test** (RF-006-01, RF-006-06)
  - `ThemeMode` + `ThemeModeTest`; `theme_mode` en keys; Flow + `setThemeMode` en `PreferencesRepository`.
  - Validado: `./gradlew test` OK.
- [x] **T2 — Settings Switch + KvasirTheme + MainActivity** (RF-006-02…05)
  - `SettingsViewModel`/`SettingsScreen` Switch «Tema oscuro»; `KvasirTheme(darkTheme)` sin sistema; MainActivity observa VM; sin `setDefaultNightMode`.
  - Validado: `./gradlew test assembleDebug` OK; sin llamadas a `setDefaultNightMode`. Checklist dispositivo pendiente.

## Reglas

- Una tarea por sesión de implementación.
- Citar `006` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test` |
| T2 | `./gradlew test assembleDebug` + checklist: toggle recompone; sin recrear Activity; grep `setDefaultNightMode` vacío |

## Checklist manual acumulada (cierre 006)

Cubrir RF-006-02…05 en dispositivo/revisión. RF-006-01, 06: unitarias en T1.
