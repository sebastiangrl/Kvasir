# Spec 014 — Rachas e historial de hábitos

**Estado:** aprobada  
**Rama:** `spec/014-habit-streaks`

## Problema

Los hábitos (005) solo recuerdan **el día actual**: al cambiar `epochDay` se vacían los cumplidos y se pierde el pasado. El usuario quiere **rachas** (días consecutivos cumplidos) y un **historial** mínimo, sin convertir Kvasir en un habit-tracker pesado ni añadir notificaciones.

## Fuera de alcance

Notificaciones / recordatorios, gráficos elaborados, export CSV, sincronización en red, reordenar hábitos con drag, Room, deps nuevas (kotlinx.serialization). No cambiar favoritas, scrubber Niagara (013), calendario ni tema.

**Decisiones de producto:**

1. **Historial** — persistir, por hábito, los `epochDay` en que estuvo cumplido. Clave DataStore aditiva (p. ej. `habit_history_json`) vía `org.json`. Antes de resetear el día (005), volcar los cumplidos de ayer al historial.
2. **Retención** — acotar a un horizonte (p. ej. **90 días** atrás); podar entradas más viejas al escribir. Evita crecer sin límite.
3. **Racha** — número de días **consecutivos** cumplidos hacia atrás desde hoy (si hoy está cumplido) o desde ayer (si hoy aún no). Cálculo puro en dominio (JVM-testeable). Racha 0 si no hay cadena.
4. **Home** — junto a cada hábito, mostrar la racha si es ≥ 1 (p. ej. `· 3` o `🔥 3` — preferir texto sobrio: `3 días` / solo el número). No inventar filas de historial en Home.
5. **Ajustes** — vista mínima de historial reciente por hábito (p. ej. últimos 7 días como marcas cumplido/no) **o** solo el número de racha; default: **racha en Home + últimos 7 días en Ajustes** junto a cada hábito.
6. **Alta/baja** — al eliminar un hábito, borrar su historial. Al crear, historial vacío.
7. **Migración** — instalaciones 005 sin historial: mapa vacío; rachas empiezan a construir desde ahora (no reconstruir el pasado).
8. **Capas** — Compose no llama a DataStore; observa ViewModel.
9. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-014-01 — Esquema historial

El sistema debe persistir el historial de cumplidos por hábito en DataStore Preferences (JSON/`org.json`), con clave aditiva nueva, sin romper `habits_json` ni `habit_day_state_json`.

### RF-014-02 — Volcado al cambiar de día

Cuando el day state se rolle a un nuevo `epochDay`, el sistema debe incorporar al historial los ids cumplidos del día que termina, antes de dejar el estado de hoy vacío.

### RF-014-03 — Racha

El sistema debe calcular la racha actual de cada hábito a partir del historial y del estado de hoy, según la regla de consecutivos acordada.

### RF-014-04 — Home

El sistema debe mostrar en Home la racha de cada hábito cuando sea ≥ 1, sin recomponer el reloj a 1 Hz de más.

### RF-014-05 — Ajustes historial breve

El sistema debe mostrar en Ajustes, por hábito, un resumen de los últimos 7 días (cumplido / no) y/o la racha, sin pantalla separada obligatoria de analytics.

### RF-014-06 — Retención

El sistema debe podar del historial entradas anteriores al horizonte de retención al escribir.

### RF-014-07 — Borrado de hábito

Cuando el usuario elimine un hábito, el sistema debe eliminar también su historial asociado.

### RF-014-08 — Capas y deps

Los `@Composable` no deben llamar a DataStore. Sin dependencias nuevas.

## Copy de UI

- Racha en Home: p. ej. sufijo ` · 3` o `3` junto al label (español; sin copy largo).
- Ajustes: sin vacío inventado de historial; si no hay datos, no mostrar marcas falsas de días futuros.

## Impacto en rendimiento

Esta spec **toca el proceso Home** (solo UI de hábitos + lectura prefs).

- Historial acotado (90 días × N hábitos pequeños); JSON en DataStore, no en cada frame del reloj.
- Cálculo de racha en dominio al emitir estado; lista corta.
- RSS idle &lt; 80 MB; cold start / `onNewIntent` metas intactas.
- Sin Bitmap / sin deps.

## Persistencia local

- Nueva clave aditiva (p. ej. `habit_history_json`): mapa `habitId` → array de `epochDay` (o lista de pares).
- Migración: default vacío si falta la clave.
- Compatibilidad: 005 sin historial arranca con rachas 0; favoritas y day state intactos.
- Retención: podar &lt; hoy − 90 días.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-014-01 | JSON encode/decode historial | — | — |
| RF-014-02 | roll + append historial | — | cruzar medianoche / cambiar día |
| RF-014-03 | racha casos borde (hoy/ayer/hueco) | — | — |
| RF-014-04 | — | — | Home muestra racha ≥ 1 |
| RF-014-05 | — | — | Ajustes 7 días |
| RF-014-06 | poda horizonte | — | — |
| RF-014-07 | borrar hábito limpia historial | — | — |
| RF-014-08 | — | — | sin DataStore en Compose; diff Gradle |

## Criterio de hecho

- Historial persistido + rachas correctas; Home y Ajustes según RF.
- Day roll ya no pierde el pasado (dentro del horizonte).
- Sin deps; presupuesto Home intacto.
- PR único en `spec/014-habit-streaks`.
