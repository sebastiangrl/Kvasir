# Tareas 017 — Badges de notificación

**Spec:** `specs/017-badges/spec.md`  
**Plan:** `specs/017-badges/plan.md`  
**Rama:** `spec/017-badges`

## Recorte

- [x] **T1 — Repo + listener + Manifest + ops JVM** (RF-017-01, RF-017-02 parcial, RF-017-07, RF-017-08)
  - `NotificationBadgeRepository`, `KvasirNotificationListenerService`, merge/recompute testeable, Manifest.
  - Sin UI Home/Ajustes aún (API lista).
  - Validación: `./gradlew test assembleDebug`

- [x] **T2 — Hub de permisos (listener)** (RF-017-03, RF-017-02)
  - Estado + CTA `ACTION_NOTIFICATION_LISTENER_SETTINGS` en Permisos e Inicio; distinguir de POST_NOTIFICATIONS.
  - Validación: `./gradlew test assembleDebug`

- [x] **T3 — Punto en favoritas Home** (RF-017-04, RF-017-05, RF-017-06)
  - Combine badges en `HomeViewModel`; `•` solo en filas favoritas ★; no scrubber; reloj aislado.
  - Validación: `./gradlew test assembleDebug` + checklist

## Reglas

- Una tarea por sesión de implementación.
- Citar `017` y RF en código/tests.
- No adelantar la tarea siguiente.
- Tras la última tarea: PR; merge a `main` solo con OK explícito del usuario.

## Validación por tarea

| Tarea | Validación |
| --- | --- |
| T1 | `./gradlew test assembleDebug` |
| T2 | `./gradlew test assembleDebug` |
| T3 | `./gradlew test assembleDebug` + checklist |

## Checklist manual acumulada (cierre 017)

- Sin acceso listener → sin puntos en Home.
- Habilitar en hub → favorita con notif muestra ` •`.
- Scrubber A–Z / búsqueda sin badges.
- Quitar/dismiss notif → desaparece el punto (best-effort).
- Sin deps; Compose sin listener directo.
