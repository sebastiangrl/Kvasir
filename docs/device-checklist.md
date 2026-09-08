# Checklist de dispositivo (v1)

Smoke manual de las specs **001–006**. Spec **007** / RF-007-02. Ejecutar tras instalar un APK (debug de Actions o release firmado) y elegir Kvasir como app de Inicio.

UI de la app en **español**.

## Preparación

- [ ] APK instalado; Kvasir aparece en el selector de Inicio y se puede elegir.
- [ ] Se puede volver a otra app de Inicio desde Ajustes del sistema.
- [ ] Desinstalar Kvasir no deja el teléfono sin Home (el SO reasigna).

## 001 — Home esqueleto

- [ ] Home muestra reloj y fecha (locale del dispositivo).
- [ ] Pulsar Home desde otra app vuelve a Kvasir sin sensación de proceso nuevo.
- [ ] Atrás en Home **no** cierra / no saca de la pantalla de inicio.
- [ ] Si Kvasir no es Inicio por defecto: CTA para abrir el selector del SO; tras elegir Kvasir, el CTA desaparece.
- [ ] Sin lista inventada de apps en el empty state inicial (cuando no hay favoritas).

## 002 / 003 — Apps y favoritas

- [ ] En **Ajustes**, el catálogo de apps instaladas se lista (texto); se puede marcar/desmarcar favoritas.
- [ ] Favoritas persisten tras matar el proceso / reiniciar la app.
- [ ] Home muestra **solo** favoritas (labels); tap lanza la app.
- [ ] Sin favoritas resolubles: empty en Home (sin inventar filas).
- [ ] Home **no** muestra la lista completa «todas las instaladas» (eso va en overlay / Ajustes).
- [ ] Instalar o desinstalar una app actualiza el catálogo sin reiniciar el proceso (cuando aplique).

## 004 — Overlay scrubber A–Z

- [ ] Swipe-up abre el overlay con lista alfabética (+ `#` si aplica).
- [ ] Scrubber filtra por letra; letra vacía muestra empty.
- [ ] Tap en una app del overlay la lanza.
- [ ] Cierra con Atrás, swipe-down, al lanzar, o al pulsar Home (`onNewIntent`).
- [ ] Usar el overlay **no** añade ni quita favoritas.

## 005 — Hábitos del día

- [ ] Home muestra hábitos con check del día (o empty si no hay).
- [ ] Toggle de check persiste tras matar el proceso.
- [ ] En Ajustes: alta / baja / renombrar hábitos.
- [ ] Tras cambio de día (o al día siguiente), los checks del día anterior no quedan marcados.

## 006 — Tema

- [ ] Switch **Tema oscuro** en Ajustes cambia claro ↔ oscuro al instante.
- [ ] El modo persiste tras matar el proceso.
- [ ] Cambiar el tema **no** recrea la Activity (sin flash de reinicio de pantalla Home).

## Capas / no regresiones rápidas

- [ ] Copy de Inicio / UI en español donde corresponda.
- [ ] Sin iconos de apps en Home ni overlay (solo texto).
