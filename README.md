# Kvasir

Launcher Android minimalista (hiperfocus). Nativo en Kotlin y Jetpack Compose. Uso personal y APK libre: **no va a Play Store**.

Home es lo que ya decidiste usar hoy: reloj y fecha, nombres de apps favoritas, hábitos del día. El resto de apps vive detrás de un overlay alfabético A–Z. Sin grilla, sin iconos, sin widgets, sin feed.

El minimalismo es interno: poca RAM residente, vuelta a Home instantánea, cero sobrecarga.

La interfaz de la app está en **español** (el idioma del sistema del teléfono puede ser otro).

## Estado del repo

Proyecto Android **presente** (Gradle, Manifest, Compose). Specs **001–006** (v1 de producto) cerradas en `main`. Spec **007** endurece docs, checklist de dispositivo y distribución release.

## v1 (cerrado)

- Home como app de Inicio (`MAIN` / `HOME` / `DEFAULT`)
- Lista de favoritas en texto + lanzar apps
- Overlay scrubber A–Z de todas las apps lanzables
- Reloj y fecha
- Hábitos diarios editables (check del día; sin historial)
- Tema claro/oscuro sin recrear la Activity

## Instalar el APK

Distribución solo por **sideload**. No hay Play Store.

### Desde GitHub Actions (debug)

1. Abre [Actions](../../actions) del repo → workflow **CI** → una corrida verde en `main` (o en tu PR).
2. Descarga el artefacto **`app-debug`**.
3. Instala el APK en el teléfono (fuente desconocida / instalar apps permitidas según el fabricante).
4. Si ya tenías un build firmado distinto, puede hacer falta **desinstalar** antes de reinstalar.

### Desde GitHub Releases (release firmado)

Cuando existan secrets de firma y un tag `v*`, el workflow de release publica un APK **release** firmado en [Releases](../../releases). Guía: [`docs/signing.md`](docs/signing.md).

### Elegir Kvasir como Inicio

Tras instalar, el sistema pedirá (o en Ajustes → apps de Inicio) que elijas **Kvasir** como app de pantalla de inicio. Debe poderse cambiar por otra en cualquier momento.

Smoke en dispositivo: [`docs/device-checklist.md`](docs/device-checklist.md).

## Stack

- Kotlin, Jetpack Compose, Material 3
- `minSdk 26`, `targetSdk 36`
- ViewModel + StateFlow
- DI manual (sin Hilt)
- DataStore Preferences (sin Room)
- Sin `QUERY_ALL_PACKAGES`: `<queries>` de `MAIN`/`LAUNCHER`

## Distribución

- CI (push/PR): APK **debug** como artefacto (`app-debug`).
- Tags `v*`: APK **release** firmado en GitHub Release (cuando haya secrets).
- Sin Play Store.

## Cómo se trabaja

Spec manda el cambio. Una spec, una rama, un PR. Merge a `main` solo con OK explícito.

- Constitución: [`docs/constitution.md`](docs/constitution.md)
- Dominio: [`docs/domain.md`](docs/domain.md)
- Flujo SDD: [`docs/sdd.md`](docs/sdd.md)
- Git/CI: [`docs/git.md`](docs/git.md)
- Checklist dispositivo: [`docs/device-checklist.md`](docs/device-checklist.md)
- Firma release: [`docs/signing.md`](docs/signing.md)
- Índice docs: [`docs/README.md`](docs/README.md)
- Agentes: [`AGENTS.md`](AGENTS.md)

## Créditos

- **Anurati** (día de la semana en Home): tipografía de [Emmeran Richard](https://www.emmeranrichard.fr/) — uso personal; detalle en [`docs/anurati.md`](docs/anurati.md).
