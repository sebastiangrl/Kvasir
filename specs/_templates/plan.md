# Plan NNN — Título

> Copiar a `specs/NNN-slug/plan.md`. Plan = CÓMO. No implementar aquí.

**Spec:** `specs/NNN-slug/spec.md`  
**Estado:** borrador | aprobado

## Enfoque

Cómo se cumple la spec en este repo (un cliente Android, sin red).

## Archivos

Paths reales según `docs/domain.md` (crear, modificar, no tocar).

```
app/src/main/java/app/kvasir/launcher/…
```

## Decisiones

Cada decisión: recomendación + alternativa descartada + por qué.

1. **…** — Recomendación: … Descartado: … porque …

## Rendimiento

Cómo se respeta el presupuesto (RAM, recomposición, arranque). Qué Composables se aíslan. Qué no se cachea (`Bitmap`, Context de Activity).

## Persistencia / Android

Migración DataStore si aplica. Cambios de Manifest/`<queries>`/intents si aplica. Si no: «N/A».

## Dependencias

Lista vacía o justificación peso vs beneficio ya aprobada en la spec.

## Tests

Qué se cubre en JVM, qué es checklist, qué instrumentado (local). Mapear a RF.

## Riesgos

Qué puede romper el Home eterno, el back, o el fps del overlay. Mitigación.
