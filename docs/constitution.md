# Constitución

Kvasir es un launcher Android hiperfocus. Un solo cliente, sin backend. Estas reglas son verificables.

1. **Spec manda.** No feature, no RF, no esquema de persistencia, no cambio de Manifest/permisos/intents sin spec aprobada.
2. **Trazabilidad.** Código y tests citan spec `NNN` y RF.
3. **Rendimiento.** Spec que toque el proceso Home declara impacto RAM / recomposición / arranque; si degrada el presupuesto de `docs/domain.md`, no entra.
4. **Lógica fuera de UI.** Dominio y estado en ViewModel/casos de uso; `@Composable` no habla con `LauncherApps` ni `PackageManager`.
5. **Deps mínimas.** Añadir librería exige peso vs beneficio en la spec; default = no añadir.
6. **Tests.** Cada RF → unitaria, instrumentada, o checklist manual verificable. No reescribir tests de paso.
7. **Idioma.** Código en inglés; specs en español; copy de UI en español.
8. **Persistencia.** Cambio de esquema DataStore declara migración y compatibilidad con instalaciones previas.
9. **Git.** Una spec = rama `spec/NNN-slug` = un PR. Merge a `main` solo con OK explícito del usuario.

Sin backend, analytics ni crash reporting de terceros salvo spec que lo justifique.
