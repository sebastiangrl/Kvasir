# Specs

Una spec = una carpeta `NNN-slug` = una rama `spec/NNN-slug` = un PR.

## Plantillas

Copiar desde `_templates/` al crear una spec:

- `spec.md` — QUÉ / POR QUÉ (EARS en español)
- `plan.md` — CÓMO (archivos, decisión + alternativa descartada)
- `tasks.md` — recorte; el agente implementa **una** tarea y para

## Numeración

Siguiente número libre en tres dígitos (`001`, `002`, …). No reutilizar números. No mezclar dos specs en el mismo PR.

## Backlog sugerido (aún no escrito)

1. `001` — Esqueleto Home: Activity Compose, Manifest HOME / `singleTask` / `onNewIntent` / Back, reloj-fecha aislado, empty state.
2. `002` — `LauncherAppsRepository`: listar, callbacks, lanzar; `<queries>`; lista plana para verificar (sin arco).
3. `003` — Favoritas en Home + DataStore.
4. `004` — Overlay scrubber A–Z (salto por letra = debe; arco = debe intentar, degradable).
5. `005` — Hábitos editables + check diario + reset por `epochDay`.
6. `006` — Tema claro/oscuro Compose-only, persistido, sin recrear Activity.

No escribir estas specs hasta que el usuario pida «especifica X» en PLAN.

## Contrato

Este repo no tiene API de red. Los únicos contratos que una spec puede tocar son el SO Android (Manifest, permisos, intents, `LauncherApps`) y DataStore local.
