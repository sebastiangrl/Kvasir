# AGENTS.md

Kvasir es un launcher Android hiperfocus (Kotlin + Jetpack Compose). Un solo cliente, sin backend. Distribución por APK, no Play Store.

Lee esto antes de tocar el repo. Flujo completo: `docs/sdd.md`. Principios: `docs/constitution.md`. Dominio: `docs/domain.md`. Git: `docs/git.md`.

## No hagas esto

- No cambies RF, features, esquema DataStore, Manifest, permisos ni intents **sin spec aprobada**.
- No mezcles specs en una rama ni en un PR.
- No implementes más de **una** tarea del recorte; para y valida.
- No añadas dependencias sin justificación peso vs beneficio en la spec. Default = no añadir.
- No llames `LauncherApps` / `PackageManager` desde un `@Composable`.
- No uses `AppCompatDelegate.setDefaultNightMode` (recrea la Activity).
- No caches `Bitmap`/iconos en v1.
- No hagas push ni merge a `main` salvo OK explícito del usuario.
- No inventes backend, analytics, crash reporting de terceros, multi-cliente ni versionado de API.

## Rendimiento

Toda spec que toque el proceso Home declara impacto en RAM residente, recomposiciones y arranque. Presupuesto en `docs/domain.md`. Si el cambio lo degrada, no entra.

## Idioma

Código en inglés. Specs en español. Copy de UI en español.

## Dónde está cada cosa

| Qué | Dónde |
| --- | --- |
| Plantillas | `specs/_templates/{spec,plan,tasks}.md` |
| Specs | `specs/NNN-slug/` |
| Skill SDD | `.cursor/skills/sdd/SKILL.md` |
| Paths de código (propuestos) | `docs/domain.md` |

El proyecto Android **no existe** hasta spec `001`. No lo crees de paso.

## Trazabilidad

Código y tests citan spec `NNN` y RF. Cada RF → unitaria, instrumentada o checklist manual. No reescribir tests de paso.

## Git

Trunk: `main`. Rama: `spec/NNN-slug`. Un PR por spec. Merge a `main` solo si el usuario lo pide.
