package app.kvasir.launcher.domain.model

/**
 * Spec 012 / RF-012-03 — next upcoming calendar instance for Home (no ContentResolver in UI).
 */
data class NextCalendarEvent(
    val title: String,
    val beginEpochMillis: Long,
    val allDay: Boolean,
    val eventId: Long?,
)
