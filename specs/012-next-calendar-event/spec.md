# Spec 012 — Próximo evento de calendario

**Estado:** aprobada  
**Rama:** `spec/012-next-calendar-event`

## Problema

El Home tipográfico (011) ya ancla el día, pero no anticipa **qué toca después**. En un launcher hiperfocus, una sola línea con el **próximo evento** del calendario del dispositivo (estilo Niagara: título · hora) reduce mirar otra app. Eso exige `READ_CALENDAR` y debe fallar en silencio si no hay permiso o no hay evento.

## Fuera de alcance

Agenda completa, crear/editar eventos, `WRITE_CALENDAR`, sincronización en red, widgets de calendario, recordatorios push, multi-cuenta UI, elegir calendarios en Ajustes, rachas, carpetas. No cambiar reloj tipográfico (011) salvo colocar la línea debajo. No Coil ni deps de calendario de terceros.

**Decisiones de producto:**

1. **Qué** — un solo **próximo evento** futuro (inicio ≥ ahora, o en curso si `end` &gt; ahora y es el más cercano a terminar / el que empieza antes; criterio: el de **inicio más próximo** entre eventos con `end` &gt; ahora).
2. **Dónde** — solo **Home**, debajo del bloque reloj (011). No overlay ni Ajustes obligatorios en 012.
3. **Copy** — título del evento + hora de inicio formateada al locale (p. ej. `Reunión · 19:30`). Si es todo el día: título + indicación de día completo / fecha según locale, sin inventar hora falsa.
4. **Horizonte** — buscar en una ventana limitada (p. ej. **próximos 14 días**). Si no hay ninguno → **no mostrar fila**.
5. **Permiso** — declarar `READ_CALENDAR` (peligroso). Pedir en runtime **una vez por proceso** al primer `onResume` de Home si aún no está concedido (solo el diálogo del SO). Si denegado o revocado → **no mostrar fila** y **no volver a pedir** en esa sesión; en cada `onResume` solo re-chequear si ya está concedido (el usuario puede habilitarlo en Ajustes del SO). Sin CTA propia en Home ni toggle en Ajustes Kvasir en 012.
6. **Best-effort** — sin permiso, sin proveedor, sin eventos, o error de consulta → **cero UI inventada** (ni “Sin eventos”, ni placeholders falsos).
7. **Tap** — al pulsar la fila, abrir el calendario del SO o la vista del evento (intent best-effort); si falla, no crashear.
8. **Capas** — `@Composable` no llama a `ContentResolver` / `CalendarContract`; consulta vía repositorio/loader en data + estado en ViewModel.
9. **Refresh** — al pasar a primer plano (`onResume` / `onNewIntent` Home) y de forma acotada (no atar al tick 1 Hz del reloj).
10. **Deps** — default = no añadir.

## Requisitos (EARS)

### RF-012-01 — Manifest

El sistema debe declarar el permiso `android.permission.READ_CALENDAR` en el Manifest.

### RF-012-02 — Runtime

Cuando el permiso no esté concedido, el sistema no debe leer el calendario. Debe poder solicitarlo en runtime según la decisión de producto (sin spamear al usuario).

### RF-012-03 — Mostrar próximo

Mientras el permiso esté concedido y exista un próximo evento en el horizonte, el sistema debe mostrar en Home, bajo el reloj, una línea con título y hora (o marcador de día completo) según locale.

### RF-012-04 — Silencio

Si no hay permiso, no hay eventos en el horizonte, o la consulta falla, el sistema no debe mostrar una fila inventada ni un empty state de calendario.

### RF-012-05 — Capas

Los `@Composable` no deben consultar `CalendarContract` / `ContentResolver` directamente; observan estado del ViewModel/dominio.

### RF-012-06 — Aislamiento

La actualización del evento no debe forzar recomposición del reloj a 1 Hz ni del resto de Home más de lo necesario (estado propio / collect acotado).

### RF-012-07 — Tap

Cuando el usuario pulse la fila del próximo evento, el sistema debe intentar abrir el calendario o el evento vía intent del SO, sin crashear si no hay handler.

### RF-012-08 — Sin escritura

El sistema no debe declarar ni usar `WRITE_CALENDAR` ni modificar eventos.

## Copy de UI

- Fila evento: `{título} · {hora o día completo}` (título viene del proveedor; hora/fecha por locale).
- Sin copy de empty de calendario en Home (RF-012-04).
- Sin CTA de permiso en Home (el diálogo es solo el del SO).

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- **RAM idle:** una consulta puntual + un modelo pequeño en memoria; sin cachear toda la agenda. Techo RSS idle &lt; 80 MB intacto.
- **Recomposiciones:** no mezclar con tick del reloj; solo invalidar la fila de evento al refrescar.
- **Arranque / `onNewIntent`:** consulta en background; no bloquear primer frame del reloj. Metas &lt; 400 ms / &lt; 100 ms percibidos: la fila puede aparecer un instante después.
- **APK:** sin deps nuevas.

## Persistencia local

Sin cambio de esquema DataStore (no persistir eventos).

## Contrato con Android

- `uses-permission` `android.permission.READ_CALENDAR`.
- Runtime permission request.
- Lectura vía `CalendarContract` (Instances/Events) con Application context.
- Intent de apertura de calendario/evento al tap.
- Sin `WRITE_CALENDAR`, sin `INTERNET` por esta feature.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-012-01 | — | — | Manifest declara READ_CALENDAR |
| RF-012-02 | — | — | denegado → sin fila |
| RF-012-03 | mapper título·hora (puro) | — | concedido + evento → fila |
| RF-012-04 | — | — | sin eventos → sin fila |
| RF-012-05 | — | — | UI sin ContentResolver |
| RF-012-06 | — | — | tick reloj no dispara query |
| RF-012-07 | — | — | tap abre calendario |
| RF-012-08 | — | — | no WRITE_CALENDAR |

## Criterio de hecho

- Con permiso y evento próximo: una línea bajo el reloj.
- Sin permiso / sin evento / error: nada inventado.
- Capas correctas; sin WRITE; sin deps nuevas; presupuesto Home intacto.
- PR único en `spec/012-next-calendar-event`.
