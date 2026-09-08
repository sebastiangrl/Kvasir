# Firma de release (sideload)

Spec **007** / RF-007-04. El keystore **no** va en git. Distribución por APK sideload; **no** Play Store.

## Local

1. Crea un keystore (una sola vez; guárdalo fuera del repo o en una carpeta local ignorada):

```bash
mkdir -p keystore
keytool -genkeypair -v \
  -keystore keystore/kvasir-release.jks \
  -alias kvasir \
  -keyalg RSA -keysize 2048 -validity 10000
```

2. Copia la plantilla y rellena contraseñas:

```bash
cp keystore.properties.example keystore.properties
```

3. `keystore.properties` en la **raíz del repo** (gitignored). Claves: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. `storeFile` es ruta relativa a la raíz (p. ej. `keystore/kvasir-release.jks`).

4. Build firmado:

```bash
./gradlew assembleRelease
```

Sin `keystore.properties`, Gradle no aplica `signingConfig` de release (útil para CI debug; el job de release exige secrets).

## Debug vs release

| Build | Firma típica | Cómo se obtiene |
| --- | --- | --- |
| **debug** | debug keystore de Android | CI push/PR → artefacto `app-debug` |
| **release** | tu keystore | local con `keystore.properties`, o tag `v*` → GitHub Release |

Instalar un APK firmado con **otro** certificado sobre el mismo `applicationId` suele exigir **desinstalar** antes.

## Secrets de GitHub (CI release)

Nombres exactos (workflow [`.github/workflows/release.yml`](../.github/workflows/release.yml), tags `v*`):

| Secret | Contenido |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -w0 keystore/kvasir-release.jks` (Linux) |
| `KEYSTORE_PASSWORD` | contraseña del store |
| `KEY_ALIAS` | alias (p. ej. `kvasir`) |
| `KEY_PASSWORD` | contraseña de la key |

Sin esos secrets, el job de release debe **fallar al inicio** con un mensaje claro (no publicar APK sin firmar como Release).

## Tag → Release

1. Configura los secrets en el repo.
2. Empuja un tag `v*` (p. ej. `v0.1.0`).
3. El workflow construye `assembleRelease` y adjunta el APK al GitHub Release.

`versionName` / `versionCode` en Gradle no se bumpan solos en esta spec; el tag nombra el Release.
