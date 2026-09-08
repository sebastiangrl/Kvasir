# Spec 006 — Tema claro/oscuro

**Estado:** aprobada  
**Rama:** `spec/006-theme`

## Problema

Hoy `KvasirTheme` sigue el tema del sistema (`isSystemInDarkTheme`) y no persiste la preferencia. El dominio exige un **ThemeMode** claro/oscuro guardado en DataStore, conmutable desde Ajustes, aplicado solo con recomposición Compose — sin recrear la Activity ni usar `AppCompatDelegate.setDefaultNightMode`.

## Fuera de alcance

Modo «Sistema», tema por app, iconos, wallpaper, Material You / dynamic color avanzado, deps nuevas, cambios de Manifest. No tocar favoritas, hábitos ni overlay salvo el toggle de tema en Ajustes y el cableado de `KvasirTheme`.

**Decisiones de producto:**

1. **Modelo** — `ThemeMode` = `Light` | `Dark` (sin modo Sistema en v1).
2. **Persistencia** — DataStore Preferences, clave aditiva `theme_mode` (`"light"` | `"dark"`). Default si falta: **`light`**.
3. **UI** — Switch en Ajustes (`Tema oscuro`); al cambiar, persistir de inmediato y recomponer `MaterialTheme`.
4. **Compose-only** — `KvasirTheme(darkTheme = …)` desde estado observado; **prohibido** `setDefaultNightMode` y recrear la Activity por tema.
5. **Capas** — `@Composable` no llama a DataStore; observa ViewModel / estado inyectado.

## Requisitos (EARS)

### RF-006-01 — Esquema

El sistema debe persistir el modo de tema en DataStore Preferences bajo la clave `theme_mode` con valores `"light"` o `"dark"`. Si la clave falta, debe tratarse como `"light"`.

### RF-006-02 — Aplicación

El sistema debe aplicar el color scheme Material 3 de toda la UI Compose según el `ThemeMode` persistido.

### RF-006-03 — Toggle

Cuando el usuario cambie el Switch de tema en Ajustes, el sistema debe persistir el nuevo modo de inmediato y recomponer el tema sin recrear la Activity.

### RF-006-04 — Sin recrear Activity

El sistema no debe llamar a `AppCompatDelegate.setDefaultNightMode` ni recrear la Activity HOME para cambiar el tema.

### RF-006-05 — Capas

Los `@Composable` no deben llamar a DataStore; observan ViewModel u otro estado fuera del árbol de lectura directa de prefs.

### RF-006-06 — Compatibilidad

Instalaciones previas (001–005) sin `theme_mode` arrancan en claro. Cambios futuros de esquema exigirán migración declarada en su spec.

## Copy de UI

- Switch / etiqueta en Ajustes: `Tema oscuro`

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Lectura DataStore vía Flow (una vez en frío); cambio de tema = recomposición de scheme, no I/O por frame.
- Reloj aislado (`HomeClock`); el tick 1 Hz no debe forzar relectura de tema.
- Sin iconos/`Bitmap`. RSS idle **&lt; 80 MB**; cold start / `onNewIntent` metas de `docs/domain.md` intactas.
- Sin deps nuevas.

## Persistencia local

- Clave: `theme_mode` (`stringPreferencesKey`).
- Valores: `"light"` | `"dark"`.
- Migración: N/A (clave aditiva). Default = `"light"`.
- Compatibilidad: apps 001–005 sin clave → claro; resto de claves intactas.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents. Sin `setDefaultNightMode`.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-006-01 | Parse/default `theme_mode` | | |
| RF-006-02 | | | UI clara/oscura según modo |
| RF-006-03 | | | Toggle Ajustes persiste y recompone |
| RF-006-04 | | | Revisión: no `setDefaultNightMode`; Activity no recreada |
| RF-006-05 | | | Compose sin DataStore |
| RF-006-06 | Default light | | Instalar sobre 005 → tema claro |

## Criterio de hecho

- Tema persiste tras matar el proceso.
- Toggle en Ajustes cambia UI al instante sin recrear Activity.
- Sin `setDefaultNightMode`; sin deps nuevas; presupuesto de `docs/domain.md` respetado.
