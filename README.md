# Kvasir

Launcher Android minimalista (hiperfocus). Nativo en Kotlin y Jetpack Compose. Uso personal y APK libre: **no va a Play Store**.

Home es lo que ya decidiste usar hoy: reloj y fecha, nombres de apps favoritas, hábitos del día. El resto de apps vive detrás de un overlay alfabético A–Z. Sin grilla, sin iconos, sin widgets, sin feed.

El minimalismo es interno: poca RAM residente, vuelta a Home instantánea, cero sobrecarga.

## Estado del repo

Andamiaje Spec-Driven Development. El proyecto Android (Gradle, Manifest, código) **aún no está**; entra en spec `001`.

## v1 (cerrado)

- Home como app de Inicio (`MAIN` / `HOME` / `DEFAULT`)
- Lista de favoritas en texto + lanzar apps
- Overlay scrubber A–Z de todas las apps lanzables
- Reloj y fecha
- Hábitos diarios editables (check del día; sin historial)
- Tema claro/oscuro sin recrear la Activity

## Stack

- Kotlin, Jetpack Compose, Material 3
- `minSdk 26`, `targetSdk 36`
- ViewModel + StateFlow
- DI manual (sin Hilt)
- DataStore Preferences (sin Room)
- Sin `QUERY_ALL_PACKAGES`: `<queries>` de `MAIN`/`LAUNCHER`

## Distribución

Sideload del APK. CI publica un APK **debug** como artefacto; no firma ni publica solo. Hay que elegir Kvasir como app de Inicio en el sistema.

## Cómo se trabaja

Spec manda el cambio. Una spec, una rama, un PR. Merge a `main` solo con OK explícito.

- Constitución: [`docs/constitution.md`](docs/constitution.md)
- Dominio: [`docs/domain.md`](docs/domain.md)
- Flujo SDD: [`docs/sdd.md`](docs/sdd.md)
- Git/CI: [`docs/git.md`](docs/git.md)
- Agentes: [`AGENTS.md`](AGENTS.md)
