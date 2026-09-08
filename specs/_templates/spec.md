# Spec NNN — Título

> Copiar a `specs/NNN-slug/spec.md`. Spec = QUÉ / POR QUÉ. No CÓMO.

**Estado:** borrador | en clarificación | aprobada | en implementación | hecha  
**Rama:** `spec/NNN-slug`

## Problema

Qué duele hoy y a quién (usuario único en su teléfono).

## Fuera de alcance

Qué no entra en esta spec. No adelantar v2.

## Requisitos (EARS)

Formato en español:

- Ubicuo: *El sistema debe …*
- Evento: *Cuando …, el sistema debe …*
- Estado: *Mientras …, el sistema debe …*
- Indeseado: *Si …, el sistema debe …*

Identificadores estables: **RF-NNN-01**, **RF-NNN-02**, …

### RF-NNN-01 — …

…

## Copy de UI

Textos visibles en español. Vacío si esta spec no muestra UI.

## Impacto en rendimiento

Obligatorio. Si la spec **toca el proceso Home**, declarar:

- RAM residente (RSS idle vs presupuesto < 80 MB)
- Recomposiciones (qué se invalida; reloj aislado; overlay)
- Arranque / vuelta a Home (`onNewIntent` < 100 ms percibidos; cold start < 400 ms)

Si **no** toca el proceso Home: escribir «No toca el proceso Home» y por qué.

Nada entra si degrada el presupuesto de `docs/domain.md`.

## Persistencia local

Si toca DataStore: claves/esquema, migración, compatibilidad con instalaciones previas.

Si no toca persistencia: «Sin cambio de esquema».

## Contrato con Android

Si toca Manifest, permisos, `<queries>` o intents: listar el cambio.

Si no: «Sin cambio de Manifest, permisos ni intents».

## Dependencias

Default = no añadir. Si se propone una librería: peso vs beneficio. Si no: «Sin dependencias nuevas».

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-NNN-01 | | | |

Instrumented solo si aporta. No reescribir tests de paso.

## Criterio de hecho

Lista verificable. Incluir el presupuesto de rendimiento si aplica.
