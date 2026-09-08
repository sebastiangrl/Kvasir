package app.kvasir.launcher.data.settings

/**
 * Spec 016 / RF-016-04 — Snapshot of permission / default-Home status for the Settings hub.
 */
data class PermissionsStatus(
    val isDefaultHome: Boolean,
    val calendarGranted: Boolean,
    /** On API &lt; 33 notifications are not a runtime permission → treated as granted. */
    val notificationsGranted: Boolean,
    /** True when the platform exposes exact-alarm scheduling checks (API 31+). */
    val exactAlarmApplicable: Boolean,
    /** Meaningful when [exactAlarmApplicable]; otherwise ignored. */
    val exactAlarmGranted: Boolean,
)
