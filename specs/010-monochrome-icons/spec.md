# Spec 010 — Iconos monocromos

**Estado:** aprobada  
**Rama:** `spec/010-monochrome-icons`

## Problema

Home y overlay solo muestran texto. Con muchas favoritas o apps en el scrubber, reconocer apps a ojo es más lento. El usuario quiere los **iconos reales de cada app** con **tinte monocromo** (estilo hiperfocus / línea blanca-negra), sin packs de iconos ni cache agresiva de `Bitmap` que rompa el presupuesto de RAM.

## Fuera de alcance

Reloj tipográfico grande (spec futura). Próximo evento de calendario (spec futura). Icon packs / temas de iconos de terceros. Iconos a color sin tinte. Grilla tipo launcher clásico. Carpetas, badges, widgets. Cache en disco de iconos. Coil/Glide u otras deps de imagen salvo justificación fuerte (default = no). Ajustes: el catálogo de favoritas puede seguir solo texto en esta spec.

**Decisiones de producto:**

1. **Fuente** — icono del SO por componente (`LauncherApps` / drawable de actividad), no librería SVG web.
2. **Estilo** — tinte monocromo: en tema oscuro → tinte claro; en tema claro → tinte oscuro (según `ThemeMode` ya persistido).
3. **Dónde** — **Home** (favoritas) y **overlay** (lista filtrada). No obligatorio en Ajustes en 010.
4. **Tamaño** — icono pequeño fijo junto al label (p. ej. ~24–32 dp); lista sigue siendo fila de texto + icono, no grilla.
5. **Carga** — perezosa / por fila visible; al cerrar overlay no retener bitmaps grandes. Preferir `Drawable` + `ColorFilter` / tint Compose; **prohibido** cachear `Bitmap` a resolución de launcher en un map global ilimitado.
6. **Capas** — `@Composable` no llama a `LauncherApps` / `PackageManager`; obtiene iconos vía ViewModel / loader inyectado (mismo patrón que lanzar apps).
7. **Deps** — default = no añadir.

## Requisitos (EARS)

### RF-010-01 — Home

El sistema debe mostrar, junto a cada favorita en Home, el icono de esa app con tinte monocromo según el tema actual.

### RF-010-02 — Overlay

El sistema debe mostrar, junto a cada app listada en el overlay, el icono de esa app con el mismo criterio de tinte monocromo.

### RF-010-03 — Fuente del icono

El sistema debe obtener el icono desde el SO para el componente de la `InstalledApp` (no un asset inventado ni un pack externo).

### RF-010-04 — Tinte según tema

Mientras el `ThemeMode` sea oscuro, el tinte del icono debe ser claro; mientras sea claro, el tinte debe ser oscuro.

### RF-010-05 — Capas

Los `@Composable` no deben llamar a `LauncherApps` ni `PackageManager` para cargar iconos; observan estado / callbacks / modelos ya resueltos fuera de UI.

### RF-010-06 — Memoria

El sistema no debe mantener una cache ilimitada de `Bitmap` de iconos. Debe respetar el presupuesto de `docs/domain.md` (RSS idle &lt; 80 MB con overlay cerrado; scrubber usable; cold start / `onNewIntent` metas intactas). Al cerrar el overlay, no debe retener bitmaps de iconos del overlay.

### RF-010-07 — Sin deps nuevas

El sistema no debe añadir librerías de carga de imágenes salvo que el plan justifique peso vs beneficio; default = APIs del SDK + Compose.

## Copy de UI

N/A (sin textos nuevos).

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Iconos solo en filas visibles / listas montadas; overlay perezoso como 004.
- Reloj aislado: el tick 1 Hz no debe forzar recarga de iconos.
- Sin cache global ilimitada de `Bitmap`; preferir drawable tintado o decodificación acotada.
- RSS idle (overlay cerrado) **&lt; 80 MB**; scrubber p95 &lt; 16 ms no debe degradarse de forma inaceptable; cold start / `onNewIntent` metas intactas.

## Persistencia local

Sin cambio de esquema DataStore (iconos no se persisten).

## Contrato con Android

Sin cambio de Manifest HOME / `<queries>` / permisos nuevos. Sigue bastando la visibilidad `MAIN`/`LAUNCHER` ya declarada.

## Dependencias

Sin dependencias nuevas (default).

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-010-01 | | | Favoritas muestran icono tintado |
| RF-010-02 | | | Overlay muestra iconos tintados |
| RF-010-03 | | | Icono corresponde a la app real |
| RF-010-04 | | | Claro/oscuro cambia tinte |
| RF-010-05 | | | Revisión: Compose sin PM/LauncherApps para iconos |
| RF-010-06 | | | Overlay cerrado no retiene bitmaps; RSS ok |
| RF-010-07 | | | Sin deps nuevas en Gradle |

## Criterio de hecho

- Home y overlay muestran iconos reales monocromos tintados según tema.
- Capas y presupuesto de rendimiento respetados; sin icon pack; sin cache `Bitmap` ilimitada.
- Reloj tipográfico y calendario quedan fuera (specs futuras).
