# Spec 007 — Endurecimiento v1

**Estado:** aprobada  
**Rama:** `spec/007-v1-hardening`

## Problema

Las specs 001–006 ya entregan un launcher usable por sideload, pero el README sigue describiendo el repo como andamiaje sin proyecto Android, no hay checklist de dispositivo consolidada, y la distribución se limita al APK **debug** de CI (sin GitHub Release ni APK **release** firmado). Eso dificulta probar y compartir builds de forma fiable.

## Fuera de alcance

Búsqueda, gestos extra, iconos, carpetas, rachas/historial de hábitos, widgets, badges, work profile, atajos, per-app theming, i18n. No cambiar RF de producto de Home (favoritas, overlay, hábitos, tema) salvo documentación. No publicar en Play Store.

**Decisiones:**

1. **README** — reflejar v1 real; instrucciones para bajar APK desde Actions; elegir Kvasir como Home; UI en español.
2. **Checklist** — documento de smoke en dispositivo cubriendo 001–006 (p. ej. `docs/device-checklist.md`).
3. **Release** — en tags `v*`, workflow que construye APK release firmado y lo publica como GitHub Release (cuando existan secrets). Keystore **fuera** del repo; secrets GitHub + guía local (`keystore.properties` gitignored).
4. **CI debug** — el artefacto debug en push/PR se mantiene.
5. **Firma sin secrets** — el job de release falla o se salta con mensaje claro; no subir keystore al git.

## Requisitos (EARS)

### RF-007-01 — README

El sistema de documentación del repo debe describir el estado real de v1 (proyecto Android presente), cómo obtener e instalar el APK (Actions y/o Release), y que hay que elegir Kvasir como app de Inicio.

### RF-007-02 — Checklist dispositivo

El repositorio debe incluir una checklist de prueba en dispositivo que cubra el smoke de las specs 001–006 (Home, favoritas, overlay, hábitos, tema, Back/`onNewIntent`).

### RF-007-03 — Release firmado

Cuando se publique un tag que coincida con `v*`, el CI debe poder generar un APK **release** firmado y adjuntarlo a un GitHub Release, si los secrets de firma están configurados.

### RF-007-04 — Secretos y keystore

El sistema no debe versionar el keystore ni contraseñas. Debe documentar cómo crear el keystore, los secrets de GitHub y el uso local de `keystore.properties` (ignorado por git).

### RF-007-05 — Límites de distribución

El sistema no debe publicar en Play Store. El contrato Manifest HOME de producto (001) debe permanecer intacto; esta spec no introduce cambios de intent HOME.

## Copy de UI

N/A (esta spec no añade pantallas de producto). Textos de README/checklist en español.

## Impacto en rendimiento

**No toca el proceso Home** en runtime de producto. Solo docs, CI/Release y configuración de firma de build. El presupuesto de `docs/domain.md` no se degrada por esta spec.

## Persistencia local

Sin cambio de esquema.

## Contrato con Android

Sin cambio de Manifest, permisos ni intents. Sin Play Store.

## Dependencias

Sin dependencias nuevas en la app. Acciones de GitHub en workflows: justificar en el plan (peso vs beneficio operativo).

## Trazabilidad de tests

| RF | Unitaria | Instrumentada | Checklist manual |
| --- | --- | --- | --- |
| RF-007-01 | | | README revisado / enlaces correctos |
| RF-007-02 | | | Checklist usable en dispositivo |
| RF-007-03 | | | Tag de prueba → Release con APK (con secrets) |
| RF-007-04 | | | Keystore no en git; docs de secrets |
| RF-007-05 | | | Sin Play Store; Manifest HOME intacto |

## Criterio de hecho

- README actualizado y usable para instalar/probar.
- Checklist 001–006 publicada.
- Pipeline de Release firmado documentado y operativo con secrets.
- Keystore fuera del repo; debug CI intacto; sin Play Store.
