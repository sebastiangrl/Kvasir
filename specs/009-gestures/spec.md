# Spec 009 — Gestos Home (panel sistema + info de app)

**Estado:** aprobada  
**Rama:** `spec/009-gestures`

## Problema

En Home, swipe-up ya abre el overlay (004), pero swipe-down no hace nada: el usuario no puede abrir notificaciones / ajustes rápidos sin salir del gesto habitual de launcher. Tampoco hay forma rápida de abrir la ficha del sistema de una app (info / desinstalar) desde Home u overlay sin pasar por Ajustes del SO a mano.

## Fuera de alcance

Doble toque para apagar pantalla, marcar/desmarcar favoritas con long-press, iconos/`Bitmap`, carpetas, atajos de launcher, widgets, gestos horizontales, per-app theming, i18n. No cambiar DataStore ni el contrato HOME (`MAIN`/`HOME`/`DEFAULT`). No inventar menú propio de desinstalación (el SO lo ofrece en la ficha de la app).

**Decisiones de producto:**

1. **Swipe-down en Home** — abrir el panel del sistema. Preferir **ajustes rápidos (quick settings)**; si el SO no lo permite, **caer a notificaciones**; si ninguno funciona, no-op seguro (Home no tumba).
2. **Conflicto con overlay** — swipe-down en overlay sigue **cerrando** el overlay (004). El panel del sistema solo se dispara con swipe-down en **Home** (overlay cerrado).
3. **Long-press** — en una favorita (Home) o en una fila del overlay: abrir la **info de la app** del sistema (`ACTION_APPLICATION_DETAILS_SETTINGS` o equivalente). Desde ahí el usuario puede desinstalar / forzar detención según el SO. No mutar favoritas.
4. **Capas** — Compose detecta el gesto y notifica al ViewModel; la expansión del panel y el intent de detalles viven fuera de `@Composable` (Activity / repo / helper). Sin `LauncherApps`/`PackageManager`/`DataStore` en Compose.
5. **Permisos** — declarar solo lo necesario y documentado (p. ej. `EXPAND_STATUS_BAR` si el plan lo exige). Sin APIs que requieran privilegio de sistema / firma OEM. Sin reflexión opaca sin fallback: si falla, no-op.
6. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-009-01 — Swipe-down Home → panel

Cuando el usuario haga swipe-down en el contenido de Home (con el overlay cerrado), el sistema debe intentar abrir el panel de **ajustes rápidos**; si no es posible en ese dispositivo/API, debe intentar abrir el panel de **notificaciones**. Si ambos fallan, el sistema no debe cerrar ni recrear la Activity HOME.

### RF-009-02 — Overlay intacto

Mientras el overlay esté abierto, el swipe-down debe seguir cerrando el overlay como en 004; no debe expandir el panel del sistema en ese gesto.

### RF-009-03 — Swipe-up intacto

El sistema debe seguir abriendo el overlay con swipe-up desde Home como en 004.

### RF-009-04 — Long-press → info de app

Cuando el usuario haga long-press sobre una favorita en Home o sobre una app en el overlay, el sistema debe abrir la pantalla de detalles de esa app en Ajustes del sistema (paquete de la `InstalledApp`).

### RF-009-05 — Sin mutar favoritas

El long-press no debe añadir ni quitar favoritas; la alta/baja permanece en Ajustes (003).

### RF-009-06 — Capas

Los `@Composable` no deben llamar a `StatusBarManager`, reflexión de status bar, `PackageManager`, `LauncherApps` ni DataStore; observan ViewModel / callbacks hacia código fuera de UI.

### RF-009-07 — Rendimiento y límites

El sistema no debe añadir dependencias nuevas ni iconos/`Bitmap`. Los gestos no deben degradar el presupuesto de `docs/domain.md` (RSS idle &lt; 80 MB; reloj aislado; cold start / `onNewIntent` metas intactas). Fallos al expandir el panel no deben tumbar el proceso Home.

## Copy de UI

N/A (gestos sin textos nuevos; la ficha de app es del sistema).

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- Solo detección de gesto + intent / llamada al sistema puntual; sin bitmaps ni I/O de disco.
- Reloj aislado; long-press no invalida el tick 1 Hz.
- RSS idle / cold start / `onNewIntent` metas de `docs/domain.md` intactas.

## Persistencia local

Sin cambio de esquema.

## Contrato con Android

- Posible permiso normal `EXPAND_STATUS_BAR` (confirmar en el plan) para expandir panel.
- Intent de detalles de app (sin permiso peligroso).
- Sin cambio del intent-filter HOME. Sin device admin.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-009-01 | | | Swipe-down Home abre QS o notificaciones / no-op seguro |
| RF-009-02 | | | Swipe-down overlay cierra overlay |
| RF-009-03 | | | Swipe-up sigue abriendo overlay |
| RF-009-04 | | | Long-press Home/overlay abre ficha del sistema |
| RF-009-05 | | | Favoritas no cambian tras long-press |
| RF-009-06 | | | Revisión: Compose sin StatusBar/PM/LauncherApps/DataStore |
| RF-009-07 | | | Sin deps; Home no tumba si el panel falla |

## Criterio de hecho

- Swipe-down en Home abre QS o notificaciones (best-effort documentado).
- Overlay: swipe-down cierra; swipe-up Home abre overlay.
- Long-press abre info de app del SO; no muta favoritas.
- Capas y presupuesto de rendimiento respetados; sin deps nuevas.
