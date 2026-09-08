# Tareas 007 — Endurecimiento v1

**Spec:** `specs/007-v1-hardening/spec.md`  
**Plan:** `specs/007-v1-hardening/plan.md`  
**Rama:** `spec/007-v1-hardening`

## Recorte

- [x] **T1 — README + checklist dispositivo** (RF-007-01, RF-007-02, RF-007-05 docs)
  - Actualizar `README.md` (estado v1 real, instalar desde Actions/Release, elegir Inicio, UI en español).
  - Crear `docs/device-checklist.md` (smoke 001–006).
  - Enlazar checklist desde `docs/README.md` / README (`docs/signing.md` en T2).
  - Validado: enlaces y cobertura 001–006; Manifest no tocado.

- [x] **T2 — Firma local + docs de secrets** (RF-007-04)
  - `.gitignore`: `keystore.properties`, `*.jks`, `*.keystore`.
  - `keystore.properties.example` + `docs/signing.md` (keystore, secrets, tag → Release, nota debug vs release).
  - `app/build.gradle.kts`: `signingConfigs` release si existe `keystore.properties`.
  - Validado: properties/keystore no trackeados; `./gradlew test assembleDebug`.

- [x] **T3 — Workflow Release + git.md** (RF-007-03, RF-007-05)
  - `.github/workflows/release.yml` en tags `v*`: fail fast sin secrets; decode keystore; `assembleRelease`; GitHub Release con APK.
  - Actualizar `docs/git.md` (CI debug vs release; sin Play Store).
  - No modificar contrato de `ci.yml` debug.
  - Validado: YAML + git.md; `ci.yml` intacto; Manifest HOME sin diff.

## Reglas

- Una tarea por sesión de implementación.
- Citar `007` y RF en comentarios de workflow/docs/Gradle donde aplique.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | README + `docs/device-checklist.md` revisados; enlaces OK |
| T2 | gitignore + example + signing.md; `./gradlew test assembleDebug` |
| T3 | `release.yml` + `docs/git.md`; checklist RF-007-03/05; `ci.yml` debug intacto |

## Checklist manual acumulada (cierre 007)

- README usable para instalar/probar.
- Checklist 001–006 completa.
- Con secrets: tag `v*` de prueba → Release + APK firmado (cuando el usuario configure secrets).
- Keystore fuera de git; sin Play Store; Manifest HOME intacto.
