# Spec 019 — Pulido visual de Home

**Estado:** hecha  
**Rama:** `spec/019-home-visual-polish`

## Problema

Home ya cubre el producto 001–018, pero en dispositivo se ve **inacabado** frente al modelo hiperfocus (Niagara como referencia de *composición*, no de marca):

1. La **barra de estado** (hora del sistema, señal, batería) tiene un fondo gris distinto al de Home.
2. Los **iconos** de apps salen como **círculos blancos opacos**; no se reconoce la silueta.
3. El **buscador** y los **checkboxes** de hábitos son demasiado cuadrados respecto al resto.
4. El **abecedario** del scrubber está muy separado; ★ queda pegado arriba; falta el **arco** marcado al arrastrar (burbuja de letra).
5. El **fondo** es plano; se quiere un **degradado** suave.
6. El bloque **día / hora / fecha / próximo evento** no tiene la jerarquía del modelo: día enorme centrado, hora a la izquierda, fecha a la derecha, evento debajo.

## Fuera de alcance

- Copiar tipografía, assets, marca o APK de Niagara (solo composición de layout).
- Fuentes de terceros / `.ttf` en el APK.
- Wallpaper fotográfico, blur de escritorio, per-app theming (descartado).
- Icon packs a color; Coil/Glide.
- Quitar búsqueda, Pomodoro, hábitos o Ajustes.
- Pins, carpetas, DataStore nuevo, permisos, Manifest HOME.
- Animaciones de reloj; toggle 12/24 h.

**Decisiones de producto:**

1. **Barra de sistema** — Home edge-to-edge: status/nav **transparentes**; iconos claros u oscuros según tema; el contenido usa insets. El fondo de la barra es el **mismo degradado** que Home (sin franja gris).
2. **Iconos** — seguir monocromos (010) pero **silueta real**: no tintar el disco de fondo de iconos adaptativos. Fallback: círculo vacío si el SO no da drawable.
3. **Redondeo** — campo de búsqueda y checkboxes con **esquinas redondeadas** (píldora / círculo), alineados al resto.
4. **Scrubber** — rail **compacto** (no estirar ★…# a toda la altura); ★ junto a las letras, no en el status bar. En arrastre: **arco** hacia la lista + **burbuja** con la letra/★ activa. Tap sigue eligiendo. Idle: rail casi recto y denso.
5. **Fondo** — degradado vertical sutil (oscuro: carbón → negro; claro: inverso). Sin imagen.
6. **Reloj** — composición: **día** (abreviatura locale, mayúsculas) dominante y centrado; **hora** a la izquierda (sin segundos); **fecha** a la derecha (mes abreviado + día, mayúsculas); **evento** (012) una línea debajo, centrada. Tipografía Material 3; tick 1 Hz aislado.

## Requisitos (EARS)

### RF-019-01 — Barra de sistema

Cuando Home (o Ajustes) esté visible, el sistema debe pintar status bar y navigation bar **sin color de franja propio**: transparentes, edge-to-edge, con iconos legibles según `ThemeMode`.

### RF-019-02 — Iconos con silueta

El sistema debe mostrar, junto a cada app en favoritas y catálogo, el **glifo** del icono de esa app con tinte monocromo (no un disco opaco del fondo adaptativo). Si no hay drawable, debe dejar el hueco vacío (sin crash).

### RF-019-03 — Formas redondeadas

El sistema debe mostrar el campo «Buscar apps» y los checkboxes de hábitos con esquinas claramente redondeadas (no rectángulos de radio ~0).

### RF-019-04 — Scrubber compacto y arco

El sistema debe mostrar el scrubber ★ + A–Z + `#` en un bloque **compacto** al borde derecho (letras juntas). Mientras el usuario arrastre el rail, el sistema debe curvar las letras hacia la lista y resaltar la selección con una burbuja. Al soltar, el arco puede relajarse; el modo elegido permanece.

### RF-019-05 — Degradado de fondo

El sistema debe pintar el fondo de Home (y Ajustes) con un degradado vertical sutil según el tema; no un color plano único.

### RF-019-06 — Composición reloj + evento

Cuando Home esté en modo ★ con query vacía, el sistema debe mostrar el bloque reloj en la composición descrita (día centrado dominante; hora izquierda; fecha derecha; evento debajo si existe). La hora **no** debe incluir segundos. El tick 1 Hz no debe recomponer favoritas, hábitos ni scrubber.

### RF-019-07 — Capas y deps

Los `@Composable` no deben llamar a `LauncherApps` / `PackageManager`. El sistema no debe añadir dependencias nuevas ni fuentes de terceros.

## Copy de UI

Sin strings nuevos obligatorios. «Buscar apps», «Ajustes», hábitos y Pomodoro se reutilizan. Día / hora / fecha / evento salen del locale.

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- **RAM:** degradado = brush; iconos siguen LRU acotada (010); silueta = bitmap pequeño por fila visible (p. ej. ~28 dp), no resolución de launcher. RSS idle &lt; 80 MB.
- **Recomposiciones:** reloj aislado; arco del scrubber solo invalida el rail (no la lista en cada pixel: letra seleccionada estable como 013).
- **Arranque / `onNewIntent`:** edge-to-edge en `onCreate`; sin I/O extra. Metas &lt; 400 ms / &lt; 100 ms percibidos intactas.
- **Scrubber:** 60 fps / p95 &lt; 16 ms en arrastre.

## Persistencia local

Sin cambio de esquema.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents. Solo tema de ventana / `enableEdgeToEdge` (colores de system bars).

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-019-01 | — | — | Status/nav sin franja gris; iconos de sistema legibles |
| RF-019-02 | — | — | Brave/WhatsApp muestran glifo, no disco blanco |
| RF-019-03 | — | — | Buscador y checkboxes redondeados |
| RF-019-04 | — | — | Rail compacto; arrastre = arco + burbuja |
| RF-019-05 | — | — | Degradado visible en claro y oscuro |
| RF-019-06 | hora SHORT; fecha MMM d | — | TUE centrado; hora izq; fecha der; evento debajo; sin segundos |
| RF-019-07 | — | — | Compose sin PM; Gradle sin deps nuevas |

## Criterio de hecho

- Barra de sistema integrada al fondo; iconos con silueta; buscador/checks redondos; scrubber compacto con arco al arrastrar; degradado; reloj al layout acordado.
- Presupuesto Home intacto; sin fuentes Niagara; sin DataStore/Manifest de producto.
- PR único en `spec/019-home-visual-polish`.
