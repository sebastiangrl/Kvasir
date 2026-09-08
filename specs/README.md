# Specs

Una spec = una carpeta `NNN-slug` = una rama `spec/NNN-slug` = un PR.

## Plantillas

Copiar desde `_templates/` al crear una spec:

- `spec.md` — QUÉ / POR QUÉ (EARS en español)
- `plan.md` — CÓMO (archivos, decisión + alternativa descartada)
- `tasks.md` — recorte; el agente implementa **una** tarea y para

## Numeración

Siguiente número libre en tres dígitos (`001`, `002`, …). No reutilizar números. No mezclar dos specs en el mismo PR.

## Hechas

| Spec | Tema |
| --- | --- |
| `001` | Esqueleto Home |
| `002` | `LauncherApps` + lista de verificación |
| `003` | Favoritas + DataStore |
| `004` | Overlay scrubber A–Z |
| `005` | Hábitos del día |
| `006` | Tema claro/oscuro |
| `007` | Endurecimiento v1 (README, checklist, Release firmado) |
| `008` | Búsqueda |
| `009` | Gestos extra |
| `010` | Iconos monocromos |
| `011` | Reloj tipográfico |
| `012` | Próximo evento de calendario |
| `013` | Home + scrubber unificado (Niagara ★ / A–Z) |
| `014` | Rachas / historial de hábitos |

## Backlog sugerido (aún no escrito)

| Spec | Tema |
| --- | --- |
| `015` | Widgets |
| `016` | Badges |
| `017` | Work profile |
| `018` | Atajos |
| `019` | Per-app theming |

**Descartado del backlog:** carpetas-grupo de apps (tipo carpeta “Social”). Sustituido por scrubber unificado (013).

No escribir estas specs hasta que el usuario pida «especifica X» en PLAN. Siguiente número libre: **015**.

## Contrato

Este repo no tiene API de red. Los únicos contratos que una spec puede tocar son el SO Android (Manifest, permisos, intents, `LauncherApps`) y DataStore local.
