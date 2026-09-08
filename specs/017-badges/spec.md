# Spec 017 — Badges de notificación

**Estado:** hecha  
**Rama:** `spec/017-badges`

## Problema

En Home, las favoritas son solo texto (e icono monocromo): **no hay señal** de que una app tenga actividad pendiente (mensajes, correo, etc.). El usuario hiperfocus quiere un aviso **mínimo** sin abrir la app ni mirar la barra de estado, sin convertir Kvasir en un launcher ruidoso con contadores.

## Fuera de alcance

- Números de no leídos / badges con cifra.
- Badges en el listado A–Z / búsqueda (solo favoritas en chrome ★).
- Badges sobre el Drawable del icono (círculo en la esquina del icono); indicador **junto al label**.
- Contenido de notificaciones (títulos, texto, acciones); solo presencia agregada por paquete.
- Work profile, atajos, per-app theming.
- Cambiar Pomodoro, hábitos, calendario, tema, hub 016 salvo añadir fila de permiso de listener.
- Dependencias nuevas.

**Decisiones de producto:**

1. **Qué** — si un paquete de una **favorita** tiene al menos una notificación activa relevante (no clearable-only spam según filtro del listener; default: cualquier notificación posted del paquete que el SO exponga al listener), mostrar un **punto** sobrio junto al label (p. ej. `WhatsApp ·` o `• WhatsApp` — preferir sufijo ` ·•` o carácter `•` al final).
2. **Dónde** — solo filas de **favoritas en Home** (modo ★, chrome de favoritas). No scrubber A–Z ni resultados de búsqueda.
3. **Fuente** — `NotificationListenerService` (permiso especial del SO: acceso a notificaciones). Sin él → **cero badges** (silencio, sin inventar puntos).
4. **Onboarding** — CTA en **Ajustes → Permisos e Inicio** (016): estado Concedido/Denegado + abrir pantalla del sistema de listeners de notificación. No pedir en bucle en Home.
5. **Privacidad** — el listener solo mantiene un **set de package names** con notificación activa; no persistir títulos ni bodies; no DataStore de contenido.
6. **Capas** — `@Composable` no implementa el listener ni lee `StatusBarNotification`; observa un Flow/estado del ViewModel (set de packages con badge).
7. **Rendimiento** — actualizaciones solo cuando el listener notifica posted/removed; no polling; no invalidar el reloj 011.
8. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-017-01 — Servicio listener

El sistema debe declarar e implementar un `NotificationListenerService` que actualice el conjunto de paquetes con notificación activa, sin almacenar el contenido de las notificaciones.

### RF-017-02 — Permiso / silencio

Mientras el acceso de listener no esté concedido por el usuario en el SO, el sistema no debe mostrar badges inventados.

### RF-017-03 — Hub de permisos

El sistema debe mostrar en Ajustes → Permisos e Inicio el estado del acceso a notificaciones (listener) y un CTA para abrir la pantalla del sistema donde se habilita.

### RF-017-04 — Favoritas Home

Mientras el listener esté activo y una favorita tenga al menos una notificación activa de su paquete, el sistema debe mostrar un indicador de punto junto a su label en Home (chrome ★).

### RF-017-05 — Sin scrubber

El sistema no debe mostrar badges en el listado por letra ni en resultados de búsqueda.

### RF-017-06 — Capas y reloj

Los `@Composable` no deben tocar el listener ni `StatusBarNotification`. La actualización de badges no debe forzar recomposición del reloj tipográfico a 1 Hz.

### RF-017-07 — Sin esquema de contenido

El sistema no debe persistir en DataStore el texto ni el historial de notificaciones ajenas. (Sin claves nuevas salvo que el plan demuestre necesidad mínima; default = ninguna.)

### RF-017-08 — Sin deps

El sistema no debe añadir dependencias Gradle por esta feature.

## Copy de UI

- Indicador Home: punto tipográfico sobrio (p. ej. `•` o ` ·•`) junto al label; sin números.
- Ajustes permisos: `Acceso a notificaciones` (o similar); estados `Concedido` / `Denegado`; CTA `Abrir ajustes`.
- Sin empty state de badges en Home.

## Impacto en rendimiento

Esta spec **toca el proceso Home** (filas de favoritas + estado de packages).

- **RAM:** set pequeño de package names; sin Bitmap extra; listener ligado al proceso (best-effort; documentar si el SO aísla el servicio).
- **Recomposiciones:** solo filas de favoritas al cambiar el set; reloj 011 aislado.
- **Arranque / `onNewIntent`:** sin bloqueo; badges pueden aparecer cuando el listener conecte.
- Techo RSS idle &lt; 80 MB intacto; sin deps.

## Persistencia local

Sin cambio de esquema DataStore (estado de badges efímero en memoria / servicio).

## Contrato con Android

- Declarar `NotificationListenerService` en Manifest (`BIND_NOTIFICATION_LISTENER_SERVICE`).
- Intent a ajustes de acceso a notificaciones (`ACTION_NOTIFICATION_LISTENER_SETTINGS` o equivalente).
- Sin leer contenido para UI; sin `INTERNET`.
- Extiende hub 016 (fila nueva); sin permisos runtime clásicos (`POST_NOTIFICATIONS` es otro permiso — no confundir).

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-017-01 | merge posted/removed → set packages (puro si se extrae) | — | servicio registrado |
| RF-017-02 | — | — | sin acceso → sin puntos |
| RF-017-03 | — | — | hub muestra estado + abre ajustes |
| RF-017-04 | — | — | favorita con notif → punto |
| RF-017-05 | — | — | scrubber sin badges |
| RF-017-06 | — | — | sin listener en Compose; reloj estable |
| RF-017-07 | — | — | sin claves DataStore de contenido |
| RF-017-08 | — | — | diff Gradle |

## Criterio de hecho

- Punto en favoritas Home solo con listener concedido y notificación activa.
- CTA en hub de permisos; sin números; sin badges en scrubber; sin deps.
- PR único en `spec/017-badges`.
