# Git y GitHub

## Trunk

Rama principal: **`main`**.

## Una spec, una rama, un PR

- Rama: `spec/NNN-slug` (ejemplo: `spec/001-home-skeleton`).
- Un PR por spec. No mezclar specs en el mismo PR.
- El agente **no** hace push ni merge a `main` salvo que el usuario lo pida de forma explícita.
- Merge a `main` solo con OK explícito del usuario, aunque CI esté verde.

## Antes de push / antes de merge

| Momento | Exigencia |
| --- | --- |
| Antes de push | `./gradlew test` (y el build que la spec requiera) cuando exista `gradlew` |
| Antes de merge | CI verde |

Hasta spec `001` no hay Gradle: los PRs de andamiaje no ejecutan build Android.

## CI

Workflow: `.github/workflows/ci.yml`.

- Runner: `ubuntu-latest`.
- JDK 17 Temurin + cache de Gradle.
- Jobs Gradle **solo si existe `gradlew`** (el repo nació sin proyecto Android).
- Mínimo: unit tests JVM + `assembleDebug`.
- Artefacto: APK debug descargable (distribución = sideload, no Play Store).
- **No:** firmar, publicar, emulador por defecto, instrumented en CI, Playwright, goldens masivos.

Instrumented tests: locales, únicamente cuando la spec lo exija.

## Commits

- Mensajes en español o inglés, concisos, enfocados en el porqué.
- El agente no crea commits ni PRs a menos que el usuario lo pida.
- No `--no-verify`, no force-push a `main`.
