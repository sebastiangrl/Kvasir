## Spec

- Número y slug: `spec/NNN-slug`
- Spec: `specs/NNN-slug/spec.md`
- RF cubiertos:

## Checklist

- [ ] La spec está aprobada; este PR no mezcla otra spec
- [ ] Código y tests citan `NNN` y RF
- [ ] Impacto en el proceso Home declarado (RAM / recomposición / arranque) o «no toca Home»
- [ ] Persistencia: esquema + migración + compatibilidad, o sin cambio de DataStore
- [ ] Contrato Android: Manifest / permisos / intents declarados, o sin cambio
- [ ] Sin dependencias nuevas, o justificación peso vs beneficio en la spec
- [ ] Cada RF tiene unitaria, instrumentada o checklist manual
- [ ] Copy de UI en español; código en inglés

## Rendimiento

Qué cambia respecto al presupuesto de `docs/domain.md` (o N/A).

## Cómo probar

Build / `./gradlew test` / checklist en dispositivo (Home, back, overlay).

## Merge

No mergear a `main` sin OK explícito del autor, aunque CI esté verde.
