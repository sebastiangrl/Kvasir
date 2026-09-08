# Spec 018 — Atajos de apps

**Estado:** hecha  
**Rama:** `spec/018-shortcuts`

## Problema

Hoy el long-press de una app (009) solo abre la **ficha del sistema**. Muchas apps publican **atajos** (contactos, acciones rápidas) vía el framework de shortcuts de Android; el usuario hiperfocus quiere llegar a esa acción sin abrir la app entera ni salir a otro launcher, sin llenar Home de iconos sueltos.

## Fuera de alcance

- Fijar atajos como filas permanentes en Home / favoritas (pinned shortcuts en la lista ★).
- Work profile (descartado).
- Atajos de teclado / accessibility.
- Crear o editar shortcuts propios de Kvasir (solo consumir los que publica cada app).
- Cambiar badges, Pomodoro, hábitos, scrubber salvo el gesto long-press de filas de app.
- Dependencias nuevas.

**Decisiones de producto:**

1. **Qué** — atajos de app (`ShortcutInfo`) visibles vía `LauncherApps` (manifest + dinámicos; no exigir pinned en v1).
2. **Dónde** — long-press en filas de app en **Home ★ (favoritas)** y en **catálogo A–Z / búsqueda** (mismas superficies que 009).
3. **UI** — si hay ≥ 1 atajo: panel/lista ligera (sheet o diálogo Compose) con labels de atajos + fila final **Detalles de la app**. Si hay **0** atajos: comportamiento actual 009 (abrir detalles directamente, sin panel vacío).
4. **Tap atajo** — `LauncherApps.startShortcut` (o API equivalente); si falla, no crashear.
5. **Límite** — mostrar como máximo **5** atajos (orden del sistema / ranking del publisher); el resto no se lista en v1.
6. **Capas** — `@Composable` no llama a `LauncherApps` / `ShortcutManager`; carga y launch vía ViewModel + repo.
7. **Iconos de atajo** — solo **texto** del label en v1 (sin Bitmap de shortcut); coherente con minimalismo.
8. **Deps** — sin dependencias nuevas.

## Requisitos (EARS)

### RF-018-01 — Consulta

Cuando el usuario haga long-press en una app (favorita o catálogo), el sistema debe consultar los atajos de esa app vía `LauncherApps` (perfil primario), sin hacerlo desde `@Composable`.

### RF-018-02 — Sin atajos

Si no hay atajos disponibles, el sistema debe abrir los detalles de la app como en 009 (sin panel vacío inventado).

### RF-018-03 — Con atajos

Si hay al menos un atajo, el sistema debe mostrar una lista de hasta 5 labels de atajo más una acción para abrir detalles de la app.

### RF-018-04 — Lanzar

Cuando el usuario elija un atajo, el sistema debe intentar iniciarlo vía `LauncherApps`; si falla, no debe tumbar el proceso Home.

### RF-018-05 — Capas

Los `@Composable` no deben llamar a `LauncherApps` ni `ShortcutManager` directamente.

### RF-018-06 — Sin pins en Home

El sistema no debe añadir en esta spec filas permanentes de atajos fijados en la lista de favoritas.

### RF-018-07 — Sin deps

El sistema no debe añadir dependencias Gradle por esta feature.

## Copy de UI

- Fila de detalles: `Detalles de la app` (o reutilizar copy existente si hay).
- Labels de atajo: los que publica la app (pueden venir en el idioma de la app).
- Sin empty de “Sin atajos” (RF-018-02).

## Impacto en rendimiento

Esta spec **toca el proceso Home** (gesto + panel puntual).

- Consulta de shortcuts **bajo demanda** en long-press (no en cada refresh del catálogo).
- Sin Bitmap de shortcuts; sin cache disco.
- Reloj 011 aislado; panel efímero.
- RSS idle &lt; 80 MB intacto.

## Persistencia local

Sin cambio de esquema DataStore.

## Contrato con Android

- Uso de `LauncherApps.getShortcuts` / `startShortcut` (APIs de launcher; perfil primario).
- Sin permisos Manifest nuevos esperados; sin `QUERY_ALL_PACKAGES`.
- Long-press sigue pudiendo abrir `ACTION_APPLICATION_DETAILS_SETTINGS` (009) vía la fila de detalles o el fallback sin atajos.
- Sin work profile.

## Dependencias

Sin dependencias nuevas.

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-018-01 | mapper ShortcutInfo → label/id (puro si se extrae) | — | long-press consulta |
| RF-018-02 | — | — | app sin atajos → detalles |
| RF-018-03 | tope 5 | — | panel con atajos + detalles |
| RF-018-04 | — | — | tap atajo lanza / no crash |
| RF-018-05 | — | — | sin LauncherApps en Compose |
| RF-018-06 | — | — | Home sin filas pin nuevas |
| RF-018-07 | — | — | diff Gradle |

## Criterio de hecho

- Long-press con atajos → panel; sin atajos → detalles 009.
- Launch seguro; sin pins en Home; sin deps; presupuesto Home intacto.
- PR único en `spec/018-shortcuts`.
