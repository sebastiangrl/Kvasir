# Spec 011 — Reloj tipográfico

**Estado:** aprobada  
**Rama:** `spec/011-typographic-clock`

## Problema

El reloj de Home (001) es utilitario: hora y fecha en tipografía Material estándar, apiladas. Falta la lectura **hiperfocus** estilo Niagara: el **día de la semana** como ancla tipográfica grande, con hora y fecha como apoyo, sin widgets ni calendario.

## Fuera de alcance

Próximo evento de calendario (012). Fuentes descargadas / packs tipográficos de terceros. Ajustes de formato de reloj (12/24 h custom, zona fija). Widgets. Wallpaper. Badges. Cambiar favoritas, hábitos, overlay, iconos (010). DataStore nuevo. Animaciones de reloj.

**Decisiones de producto:**

1. **Jerarquía** — el **día de la semana** (abreviatura corta del locale del dispositivo, p. ej. `MAR` / `TUE`) es el elemento dominante; la **hora** es secundaria grande; la **fecha** (día + mes, o equivalente del locale) es terciaria / variante.
2. **Layout** — fila o bloque compacto en la zona superior de Home (reloj sigue siendo el primer bloque visual); no desplazar favoritas/hábitos fuera de scroll razonable.
3. **Locale** — formato según locale del dispositivo (como 001). Sin copy fijo en español forzado sobre el locale del SO.
4. **12/24 h** — sigue la preferencia/locale del sistema (sin toggle en Ajustes en esta spec).
5. **Tipografía** — escalar / estilar con tipografía Material 3 del tema (display/title); **sin** dependencia de fuente nueva ni archivo `.ttf`/`.otf` en APK salvo justificación fuerte (default = no).
6. **Aislamiento** — tick 1 Hz en Composable aislado: no recomponer el resto de Home (hereda RF-001-06).
7. **Color** — `onSurface` / `onSurfaceVariant` del tema (006); sin glow ni efectos.

## Requisitos (EARS)

### RF-011-01 — Día dominante

El sistema debe mostrar en Home la abreviatura del día de la semana como el elemento tipográfico dominante del bloque reloj.

### RF-011-02 — Hora y fecha

El sistema debe mostrar, junto o bajo el día, la hora actual y una fecha legible (según locale), con jerarquía visual inferior al día.

### RF-011-03 — Locale

Cuando el locale del dispositivo cambie, el sistema debe formatear día, hora y fecha con ese locale (sin cadenas hardcodeadas de un solo idioma).

### RF-011-04 — Aislamiento del tick

Mientras el reloj actualiza a ~1 Hz, el sistema no debe invalidar/recomponer el resto de Home (favoritas, hábitos, CTA Inicio, overlay cerrado).

### RF-011-05 — Sin calendario

El sistema no debe mostrar ni consultar eventos de calendario en esta spec.

### RF-011-06 — Sin deps tipográficas nuevas

El sistema no debe añadir dependencias de tipografía ni empaquetar fuentes de terceros en el APK por defecto.

## Copy de UI

Ninguno fijo: día / hora / fecha salen del formateo del locale. Sin strings de recursos nuevos obligatorios.

## Impacto en rendimiento

Esta spec **toca el proceso Home** (solo UI del bloque reloj).

- **RAM idle:** sin Bitmaps nuevos, sin DataStore, sin permisos. Impacto ~0 vs 010; techo RSS idle &lt; 80 MB intacto.
- **Recomposiciones:** solo el Composable del reloj a 1 Hz (como 001). Cambiar estilos tipográficos no debe ensanchar el scope de recomposición.
- **Arranque / `onNewIntent`:** sin trabajo extra en cold start; metas &lt; 400 ms / &lt; 100 ms percibidos intactas.
- **APK:** sin fuentes empaquetadas → sin hinchazón material.

## Persistencia local

Sin cambio de esquema.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-011-01 | format día abreviado (locale) | — | Home: día grande visible |
| RF-011-02 | format hora + fecha | — | jerarquía día &gt; hora &gt; fecha |
| RF-011-03 | tests con `Locale` ES y EN | — | — |
| RF-011-04 | — | — | tick no parpadea favoritas/hábitos |
| RF-011-05 | — | — | no hay fila de evento |
| RF-011-06 | — | — | build sin deps tipográficas nuevas |

## Criterio de hecho

- Home muestra día abreviado dominante + hora + fecha según locale.
- Tick 1 Hz aislado; presupuesto Home intacto.
- Sin calendario, sin fuentes/deps nuevas, sin cambio DataStore/Manifest.
- RF cubiertos por unitarias de formato y/o checklist.
- PR único en `spec/011-typographic-clock`.
