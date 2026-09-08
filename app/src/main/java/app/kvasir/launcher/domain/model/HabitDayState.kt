package app.kvasir.launcher.domain.model

/**
 * Spec 005 / RF-005-01, RF-005-04 — completion state for one calendar day.
 */
data class HabitDayState(
    val epochDay: Long,
    val completedIds: Set<String> = emptySet(),
)
