# Spec 001 — Esqueleto Home

**Estado:** aprobada  
**Rama:** `spec/001-home-skeleton`

## Problema

Hoy el repo no es una app. Sin Activity HOME no hay launcher que instalar, elegir como Inicio, ni presupuesto de RAM/arranque que medir. Esta spec nace el proceso Home mínimo: una pantalla con reloj/fecha, vacía de favoritas, y una pista si aún no somos la app de Inicio.

## Fuera de alcance

DataStore, esquema local, `LauncherApps`, `<queries>`, lista de apps, favoritas reales, overlay/scrubber, hábitos, tema persistido, Hilt, iconos, búsqueda, Navigation Compose, red, Play Store. No `plan.md` ni implementación en este paso de especificación.

## Requisitos (EARS)

### RF-001-01 — Proyecto

El sistema debe ser una app Android Gradle de un solo módulo, una Activity Compose, `minSdk 26`, `compileSdk`/`targetSdk 36`.

### RF-001-02 — Identidad

El sistema debe usar `applicationId` `app.kvasir.launcher` y mostrar el label **Kvasir** en el picker de Inicio y en el cajón de apps.

### RF-001-03 — Ser Home (reversible)

El sistema debe declararse candidato a Inicio con `MAIN` + `HOME` + `DEFAULT`, `launchMode=singleTask`, `clearTaskOnLaunch=true`, `stateNotNeeded=true`, `resumeOnTaskLaunch=true`, Activity exportada. El sistema no debe registrarse como administrador de dispositivo ni impedir que el usuario elija otra app de Inicio en Ajustes. Si el usuario desinstala Kvasir, no debe quedar ningún privilegio que deje el teléfono sin Home (el SO reasigna Inicio).

### RF-001-04 — Vuelta caliente

Cuando el proceso ya está vivo y el usuario pulsa Home, el sistema debe entregar la pantalla por `onNewIntent` sin `finish()` ni recrear la tarea.

### RF-001-05 — Back

Cuando el usuario pulsa Atrás o predictive back en Home, el sistema no debe hacer `finish()` de la Activity de Inicio; debe permanecer en Home.

### RF-001-06 — Reloj

El sistema debe mostrar hora y fecha grandes. El tick debe ser 1 Hz y vivir en un Composable aislado: no recomponer el resto de Home.

### RF-001-07 — Vacío

Mientras no existan favoritas (esta spec no las implementa), el sistema debe mostrar Home sin lista de apps inventada: reloj/fecha + empty state.

### RF-001-08 — No soy Inicio

Mientras Kvasir no sea la app de Inicio predeterminada, el sistema debe mostrar el copy de «No soy Inicio» (abajo). Cuando el usuario pulse esa acción, el sistema debe abrir el **selector o ajustes de app de Inicio del SO**, no forzar el default por APIs irreversibles.

### RF-001-09 — Releer rol

Cuando la Activity vuelva a primer plano, el sistema debe volver a evaluar si es la app de Inicio y mostrar u ocultar el copy de RF-001-08.

### RF-001-10 — Capas

El sistema debe usar DI manual (`Application` + contenedor). Los `@Composable` no deben llamar a `PackageManager` (ni a `LauncherApps`). Observan estado del ViewModel/dominio.

### RF-001-11 — Ausencias

El sistema no debe incluir DataStore, Hilt, `<queries>`, `QUERY_ALL_PACKAGES`, `INTERNET`, lista de apps instaladas, overlay, hábitos, ni persistencia de tema. Tema visual: default Material 3 / sistema; no recrear la Activity.

## Copy de UI

- Empty favoritas: `Sin favoritas todavía.`
- No soy Inicio: `Elige Kvasir como app de Inicio.`
- Acción: `Elegir como Inicio`
- Reloj/fecha: formato del locale del dispositivo (no copy fijo).

## Impacto en rendimiento

Esta spec **toca el proceso Home**.

- **RAM idle:** proceso Home nace aquí. Techo **< 80 MB** RSS en gama media, overlay inexistente, sin iconos/`Bitmap`, sin DataStore. Una Activity, un árbol Compose mínimo. Si el esqueleto supera ~80 MB, no entra.
- **Recomposiciones:** solo el Composable del reloj a 1 Hz. El empty state y el copy de Inicio no se invalidan cada segundo. Sin overlay.
- **Arranque:** cold start → primer frame **< 400 ms**. `onNewIntent` **< 100 ms** percibidos; no recrear el árbol entero al volver.
- **APK debug:** alarma si se dispara por encima de ~5 MB por deps de más. BOM Compose es el grueso esperado.

## Persistencia local

Sin cambio de esquema. Sin DataStore. Nada que migrar.

## Contrato con Android

- Manifest Activity: `MAIN` + `HOME` + `DEFAULT`; `singleTask`; `clearTaskOnLaunch`; `stateNotNeeded`; `resumeOnTaskLaunch`; `exported=true`.
- Predictive back: no `finish()`; `enableOnBackInvokedCallback` permitido en Manifest para cumplir RF-001-05.
- Sin permisos peligrosos. Sin `INTERNET`. Sin `<queries>`. Sin `QUERY_ALL_PACKAGES`.
- Intent **saliente** (RF-001-08): selector/ajustes de app de Inicio del SO. No device admin. No fijar Home de forma irreversible.
- `onNewIntent` es el camino caliente (RF-001-04).

## Dependencias

Default = no añadir. Esqueleto justificado:

- **Compose BOM** (UI, Foundation, Material 3, compiler): sin esto no hay UI. Material 3 va con el BOM, no es librería extra.
- **`androidx.activity:activity-compose`:** `setContent` en una `ComponentActivity`. Evita AppCompat (y `setDefaultNightMode`).
- **Lifecycle** (`lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`): ViewModel + `StateFlow` + `collectAsStateWithLifecycle` acordados en dominio; el copy de Inicio y el empty state no viven en `remember` suelto.

No Hilt, no DataStore, no Navigation, no Coil, no Accompanist, no crash reporting.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-001-01 | | | `assembleDebug` / CI cuando exista `gradlew` |
| RF-001-02 | | | Label Kvasir en picker; `applicationId` en Gradle |
| RF-001-03 | | | Aparece como app de Inicio; se puede elegir otra; desinstalar deja Home del sistema |
| RF-001-04 | | | Abrir otra app, pulsar Home, vuelve a Kvasir sin parpadeo de proceso nuevo |
| RF-001-05 | | Solo si aporta | Atrás no cierra Home |
| RF-001-06 | Si el tick se extrae del Composable | | Segundos cambian; el copy de Inicio no parpadea |
| RF-001-07 | | | No hay lista falsa de apps |
| RF-001-08 | | | CTA abre el selector del SO |
| RF-001-09 | | | Tras elegir Kvasir y volver, el CTA desaparece |
| RF-001-10 | | | Revisión: Compose no importa PackageManager |
| RF-001-11 | | | No hay DataStore/`queries`/Hilt en Gradle |

## Criterio de hecho

- APK debug instalable.
- Kvasir sale en el picker de Inicio.
- Home muestra reloj/fecha aislados + empty state.
- CTA solo si no somos default y es reversible.
- Back no mata la Activity.
- `onNewIntent` caliente.
- RSS y tiempos dentro del presupuesto de `docs/domain.md`.
- Dependencias solo las justificadas arriba.
