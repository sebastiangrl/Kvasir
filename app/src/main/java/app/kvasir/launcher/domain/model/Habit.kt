package app.kvasir.launcher.domain.model

/**
 * Spec 005 / RF-005-01 — daily habit item (stable id + label).
 */
data class Habit(
    val id: String,
    val label: String,
)
