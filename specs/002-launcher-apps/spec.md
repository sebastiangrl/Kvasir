# Spec 002 — Listar y lanzar apps

**Estado:** aprobada  
**Rama:** `spec/002-launcher-apps`

## Problema

Con 001 el Home existe pero no ve ni abre otras apps. Sin `LauncherApps` + `<queries>` no hay launcher usable ni base para favoritas/scrubber. Hace falta el repositorio en memoria, el contrato de visibilidad con el SO, y una superficie mínima de verificación.

## Fuera de alcance

Favoritas persistidas (003), overlay/scrubber/arco (004), hábitos, tema persistido, DataStore, iconos/`Bitmap`, búsqueda, carpetas, work profile, atajos, `QUERY_ALL_PACKAGES`, Hilt, deps nuevas. No cambiar el contrato HOME de 001 (salvo añadir `<queries>`).

**Superficie:** Home de 001 se mantiene (reloj, empty de favoritas, CTA Inicio). Esta spec añade debajo una **lista plana provisional de texto** de todas las apps lanzables (tap = lanzar) para verificar el repositorio antes del overlay (004) y de favoritas (003). Sin arco, sin iconos. 003/004 la sustituirán como camino de producto; esta lista no es el diseño final del Home hiperfocus.

## Requisitos (EARS)

### RF-002-01 — Modelo

El sistema debe representar cada app lanzable como `InstalledApp` con `ComponentName` (o clave equivalente) + `label` (`String`), sin iconos ni `Bitmap`.

### RF-002-02 — Fuente

El sistema debe obtener la lista vía `LauncherApps` (`getActivityList` del perfil primario del usuario), no desde `@Composable` ni como API de UI.

### RF-002-03 — Visibilidad

El Manifest debe declarar `<queries>` para intents `MAIN` + `LAUNCHER`. El sistema no debe usar `QUERY_ALL_PACKAGES` ni permisos peligrosos nuevos.

### RF-002-04 — Exclusiones

El sistema no debe incluir en la lista el propio paquete de Kvasir. Solo el usuario primario (sin work profile en v1).

### RF-002-05 — Orden

El sistema debe ordenar la lista por `label` de forma alfabética case-insensitive según el locale del dispositivo.

### RF-002-06 — Observación

Cuando se instalen, desinstalen o cambien actividades lanzables, el sistema debe actualizar la lista en memoria mediante `LauncherApps.Callback` (sin reiniciar el proceso).

### RF-002-07 — Lanzar

Cuando el usuario pulse una fila, el sistema debe lanzar esa app con `LauncherApps.startMainActivity` (vía ViewModel/repositorio). Si el lanzamiento falla, el sistema no debe crashear el proceso Home.

### RF-002-08 — Capas

Los `@Composable` no deben llamar a `LauncherApps` ni `PackageManager`; observan estado del ViewModel.

### RF-002-09 — Superficie de verificación

El sistema debe mostrar en Home una lista plana scrolleable de labels (texto) de las apps de RF-002-01…05, además del reloj/empty/CTA de 001. Sin arco, sin iconos, sin carpetas.

### RF-002-10 — Carga

El sistema no debe bloquear el primer frame del reloj esperando el listado: la lista aparece cuando esté lista; mientras tanto no inventar filas falsas.

### RF-002-11 — DI

El repositorio debe vivir en el `AppContainer` de Application (proceso eterno), con callback registrado una vez; sin fugas de `Activity`.

## Copy de UI

- Cabecera de la lista provisional: `Apps instaladas`
- Vacío de lista (p. ej. fallo de queries): `No se encontraron apps.`
- Reloj / empty favoritas / CTA Inicio: sin cambio respecto a 001.

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- **RAM:** lista en memoria = `ComponentName` + `String` por app; **cero** `Bitmap`/iconos. Techo idle sigue **< 80 MB**. Si el listado o caches de iconos lo empujan, no entra.
- **Recomposiciones:** reloj sigue aislado a 1 Hz. La lista se invalida solo al cambiar el `StateFlow` de apps (callback), no cada segundo. Preferir lista lazy (`LazyColumn`).
- **Arranque:** primer frame del reloj no espera al listado (RF-002-10). `onNewIntent` / cold start metas de `docs/domain.md` intactas.
- **Deps:** sin librerías nuevas; no debe disparar la alarma de APK por deps.

## Persistencia local

Sin cambio de esquema. Sin DataStore. La lista no se persiste.

## Contrato con Android

- Añadir `<queries>` con `MAIN` + `LAUNCHER`.
- Usar `LauncherApps` + `Callback` + `startMainActivity`.
- Sin `QUERY_ALL_PACKAGES`, sin `INTERNET`, sin permisos peligrosos nuevos.
- No alterar intent-filter HOME / `singleTask` / back de 001.

## Dependencias

Sin dependencias nuevas. Solo APIs del SDK (`LauncherApps`) + stack ya justificado en 001.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-002-01 | Orden/exclusión/mapeo sobre datos fake (con 04–05) | | |
| RF-002-02 | | | Lista se llena vía `LauncherApps` |
| RF-002-03 | | | Manifest tiene `<queries>` MAIN/LAUNCHER; sin `QUERY_ALL_PACKAGES` |
| RF-002-04 | Exclusión del propio paquete | | |
| RF-002-05 | Orden alfabético case-insensitive | | |
| RF-002-06 | | | Instalar/desinstalar actualiza la lista sin reiniciar |
| RF-002-07 | | | Tap lanza; fallo no tumba Home |
| RF-002-08 | | | Revisión: `ui/` no importa `LauncherApps`/`PackageManager` |
| RF-002-09 | | | Lista plana de labels en Home; sin arco/iconos |
| RF-002-10 | | | Reloj visible en primer frame; lista llega después sin filas inventadas |
| RF-002-11 | | | Repo en `AppContainer`; callback una vez; sin fuga de Activity |

## Criterio de hecho

- APK con `<queries>` MAIN/LAUNCHER.
- Home muestra lista plana de labels (además de reloj/empty/CTA de 001).
- Tap lanza la app.
- Callbacks actualizan la lista sin reiniciar el proceso.
- Sin iconos/`Bitmap`.
- Reloj intacto (1 Hz aislado).
- RSS idle y arranque dentro del presupuesto de `docs/domain.md`.
- Base lista para specs 003 (favoritas) y 004 (overlay).
