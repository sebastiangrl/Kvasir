# Spec 021 — Pomodoro: registro diario, bloque visible y pantalla de bloqueo

**Estado:** hecha  
**Rama:** `spec/021-pomodoro-focus-log`

## Problema

El Pomodoro de **015** ya temporiza trabajo/descanso y avisa al fin de fase, pero:

1. **No hay registro diario** — el usuario no sabe cuántas sesiones de foco completó hoy ni puede revisar días pasados («qué tanto hiperfocus hice»).
2. **«Sesiones por ciclo» confunde** — no es el contador diario; es cuántos bloques de *trabajo* encadena el ciclo clásico antes de cerrarse (p. ej. 4×25+5). Con 50/10 y N=1, el ciclo es un solo trabajo + un descanso. Hoy la UI no deja claro esa diferencia.
3. **El bloque en Home es fácil de pasar por alto** — poco contraste/tamaño frente a favoritas y hábitos.
4. **En pantalla de bloqueo no se ve el timer** — solo hay aviso al *fin* de fase; mientras corre, hay que desbloquear para mirar el restante. Eso rompe el flujo de foco.

## Fuera de alcance

- Cambiar la máquina de fases trabajo↔descanso ni el modelo `sessionsPerCycle` (sigue siendo longitud del ciclo; solo se aclara copy).
- Contador de «minutos totales» o gráficos/heatmaps (v1 = conteo de **sesiones de trabajo completadas** por día).
- Descanso largo distinto del corto, sonidos custom, DND, Wear, sync nube.
- App Widget de Kvasir en la pantalla de inicio de *otros* launchers.
- Burbuja flotante / overlay sobre otras apps.
- Reescribir Ajustes hub (016) ni badges (017).
- Contador diario de «sesiones por ciclo» como si fueran lo mismo (son ortogonales).

**Decisiones de producto:**

1. **Sesiones por ciclo (015, se mantiene)** — entero N = trabajos por ciclo. Tras N trabajos (+ descansos de la cadena), fin de ciclo → idle. Con N=1 y 50/10: un trabajo, un descanso, ciclo completo. **No** sustituye el registro diario.
2. **Qué cuenta el registro** — solo **trabajos completados** (transición trabajo→descanso o fin de ciclo tras el último trabajo). Pausar/detener a mitad **no** suma. Los descansos no suman.
3. **Día calendario** — zona horaria del dispositivo; al pasar medianoche el contador «Hoy» arranca en 0; el día anterior queda en el historial.
4. **Dónde se ve** — Home: contador **Hoy · N** dentro del bloque Pomodoro. Ajustes → Pomodoro: lista de **días recientes** (fecha + sesiones). Sin pantalla nueva aparte.
5. **Retención** — persistir los **últimos 30 días** con conteo (podar entradas más antiguas al escribir).
6. **Bloque Home** — contenedor más visible (superficie/borde propio), tipografía del restante un poco mayor, contador del día siempre visible en ★; acciones iniciar/pausar/reanudar/detener intactas.
7. **Avisos al cambiar de fase** — al terminar trabajo: notificación clara de que **terminó el trabajo y empieza el descanso** (si la cadena arranca descanso). Al terminar descanso: aviso de que vuelve el trabajo o de ciclo completo (como 015, copy afinado).
8. **Pantalla de bloqueo / barra** — mientras la sesión esté **running** (y, si aplica, **paused**), el sistema debe mostrar una **notificación en curso** (ongoing) con fase + restante (o chronometer), visible en pantalla de bloqueo y sombra de notificaciones, sin obligar a abrir Kvasir. Al detener o idle: quitarla. Acciones mínimas en esa notificación: **Pausar/Reanudar** y **Detener** (según estado).
9. **Permisos** — reutilizar `POST_NOTIFICATIONS`; si el plan exige servicio en primer plano para fiabilidad del ongoing en API recientes, declararlo en plan (tipo acotado) — default preferido: ongoing + alarma exacta de 015 sin FGS si el SO lo permite.
10. **Capas / deps** — Compose no toca Alarm/Notify/DataStore; sin deps nuevas.

## Requisitos (EARS)

### RF-021-01 — Contador del día

El sistema debe mantener el número de sesiones de trabajo Pomodoro **completadas en el día calendario local** y mostrarlo en el bloque Pomodoro de Home (p. ej. `Hoy · 3`).

### RF-021-02 — Persistencia del historial

El sistema debe persistir en DataStore el registro por día (fecha → conteo de trabajos completados), con poda de retención acordada, compatible con instalaciones que ya tienen config/sesión 015.

### RF-021-03 — Incremento al completar trabajo

Cuando una fase de **trabajo** termine con éxito según la máquina de fases (no por Detener a mitad), el sistema debe incrementar en 1 el conteo del día actual y reflejarlo en Home/Ajustes.

### RF-021-04 — Historial en Ajustes

El sistema debe listar en la sección Pomodoro de Ajustes los días recientes del historial (fecha legible + número de sesiones), ordenados del más reciente al más antiguo. Lista vacía si nunca hubo sesiones contadas.

### RF-021-05 — Copy de «Sesiones por ciclo»

El sistema debe aclarar en Ajustes (label o texto de ayuda) que «Sesiones por ciclo» es cuántos **trabajos** encadena un ciclo, distinto del contador diario «Hoy».

### RF-021-06 — Bloque Home más visible

El sistema debe presentar el Pomodoro en Home dentro de un contenedor propio, con restante y contador del día más legibles (mayor énfasis tipográfico que en 015/020), sin degradar el presupuesto de recomposición del reloj.

### RF-021-07 — Notificación de fin de trabajo → descanso

Cuando termine un trabajo y la cadena pase a descanso, el sistema debe notificar en español que el trabajo terminó y que **empieza el descanso** (no solo un título genérico opaco).

### RF-021-08 — Notificación en curso (bloqueo)

Mientras el Pomodoro esté en marcha o en pausa, el sistema debe publicar una notificación **en curso** con fase y tiempo restante (o chronometer), visible en pantalla de bloqueo cuando el usuario tenga notificaciones ahí habilitadas. Si `POST_NOTIFICATIONS` está denegado, el timer en Home sigue; no inventar avisos falsos.

### RF-021-09 — Capas y rendimiento

Los `@Composable` no deben llamar a DataStore, `AlarmManager` ni `NotificationManager`. El tick del restante (Home + actualización de la notificación en curso) no debe forzar recomposición del reloj tipográfico ni empujar RSS idle por encima del presupuesto de `docs/domain.md`.

### RF-021-10 — Sin deps

El sistema no debe añadir dependencias Gradle por esta feature.

## Copy de UI

- Home (bloque): estado existente + `Hoy · %d`; restante más prominente.
- Ajustes — ayuda ciclo: p. ej. `Cuántos bloques de trabajo hay en un ciclo (no es el contador del día).`
- Ajustes — historial: título `Historial` / `Sesiones por día`; fila `9 sep · 4`; vacío `Sin sesiones registradas todavía.`
- Notificación fin trabajo: p. ej. `Trabajo terminado` / `Empieza el descanso`.
- Notificación en curso: título fase (`Trabajo` / `Descanso` / `Pausa`) + restante; acciones `Pausar`, `Reanudar`, `Detener`.

## Impacto en rendimiento

Esta spec **toca el proceso Home** (bloque UI + lectura de conteo).

- **RAM:** historial compacto (≤30 días); notificación del sistema fuera del proceso Compose. RSS idle &lt; 80 MB.
- **Recomposiciones:** contador del día solo al completar trabajo o cambio de día; restante aislado como 015; sin invalidar scrubber/favoritas.
- **Arranque / `onNewIntent`:** leer conteo del día desde DataStore en background; no bloquear primer frame del reloj. Metas intactas.
- **Notificación en curso:** actualizar con chronometer del framework o refrescos acotados en data layer — no un tick 1 Hz que recomonga todo Home.

## Persistencia local

- Clave aditiva nueva para historial (p. ej. JSON `pomodoro_daily_json`: mapa `yyyy-MM-dd` → Int), o equivalente documentado en plan.
- Migración: ausencia → mapa vacío; no tocar `pomodoro_config` / `pomodoro_session` salvo lectura.
- Compatibilidad: instalaciones 015/020 siguen funcionando; el conteo empieza en 0 el día del update.

## Contrato con Android

- Reutilizar canal/permiso `POST_NOTIFICATIONS` (015).
- Notificación **ongoing** + acciones PendingIntent (Pausar/Reanudar/Detener) enlazadas al controlador existente.
- Posible `FOREGROUND_SERVICE` / tipo asociado **solo si** el plan lo exige para el ongoing en el `targetSdk` actual — documentar en plan; no inventar permisos peligrosos nuevos sin necesidad.
- Sin `INTERNET`. Sin App Widget de terceros.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-021-01 | — | — | Hoy · N en Home |
| RF-021-02 | encode/decode + poda historial | — | — |
| RF-021-03 | máquina: completar trabajo incrementa; Detener no | — | completar 1 trabajo → Hoy · 1 |
| RF-021-04 | — | — | lista en Ajustes Pomodoro |
| RF-021-05 | — | — | ayuda visible bajo sesiones/ciclo |
| RF-021-06 | — | — | bloque más visible / restante mayor |
| RF-021-07 | — | — | copy fin trabajo → descanso |
| RF-021-08 | — | — | ongoing en sombra/bloqueo; quitar al Detener |
| RF-021-09 | — | — | capas; reloj aislado |
| RF-021-10 | — | — | diff Gradle sin deps |

## Criterio de hecho

- Completar trabajos suma al día; Detener a mitad no.
- Historial persistente tras matar proceso; visible en Ajustes.
- Copy de ciclo ≠ contador diario.
- Bloque Home más visible con «Hoy · N».
- Notificación de transición trabajo→descanso clara.
- Ongoing en bloqueo mientras corre/pausa; desaparece en idle.
- Presupuesto Home de `docs/domain.md` intacto; sin deps nuevas.
