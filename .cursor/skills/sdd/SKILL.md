---
name: sdd
description: >-
  Spec-Driven Development for Kvasir (Android launcher). Use when the user
  says especifica, planifica, implementa, or valida a spec; when creating or
  editing specs/NNN-slug files; when changing features, DataStore, Manifest,
  or Home-process behavior; or when the user mentions SDD, RF, or spec/NNN.
---

# SDD — Kvasir

Un solo cliente Android, sin backend. Contratos: SO Android y DataStore. Leer `AGENTS.md`, `docs/constitution.md`, `docs/domain.md`, `docs/sdd.md`, `docs/git.md`.

## Fases (no saltar)

1. **Especificación** (PLAN) — `specs/_templates/spec.md` → `specs/NNN-slug/spec.md`. QUÉ/POR QUÉ, EARS en español. Esperar aprobación.
2. **Clarificación QA** — preguntas de una en una si cambian diseño o contrato (máx. 4). Detalles menores: `[NECESITA ACLARACIÓN]`.
3. **Planificación** (PLAN) — `specs/_templates/plan.md`. CÓMO: archivos reales, decisión + alternativa descartada. No código.
4. **Tareas** — `specs/_templates/tasks.md`. Esperar OK del recorte.
5. **Implementación** — **una** tarea, parar, validar.
6. **Validación** — cada RF tocado: unit / instrumented / checklist. Spec cerrada → PR. Merge a `main` solo con OK explícito del usuario.

## Extra obligatorio en cada spec

- **Home:** impacto RAM / recomposición / arranque vs presupuesto en `docs/domain.md`, o «No toca el proceso Home».
- **DataStore:** esquema + migración + compatibilidad, o «Sin cambio de esquema».
- **SO:** Manifest / permisos / `<queries>` / intents, o «Sin cambio de Manifest, permisos ni intents».
- **Deps:** justificación peso vs beneficio, o «Sin dependencias nuevas». Default = no añadir.

No checkboxes de multi-cliente ni versionado de API.

## Git

Rama `spec/NNN-slug`. Un PR por spec. No push ni merge a `main` salvo que el usuario lo pida. Código en inglés; specs y copy de UI en español.

## Código

`@Composable` no llama a `LauncherApps` ni `PackageManager`. No `setDefaultNightMode`. No iconos/`Bitmap` en v1. Citar NNN y RF. No crear el proyecto Android fuera de spec `001`.
