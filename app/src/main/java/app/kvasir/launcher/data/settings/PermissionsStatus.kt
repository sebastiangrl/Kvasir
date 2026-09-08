package app.kvasir.launcher.data.settings

/**
 * Spec 016 / RF-016-04 + Spec 017 / RF-017-03 —
 * Snapshot of permission / default-Home / notification-listener status for the Settings hub.
 */
data class PermissionsStatus(
    val isDefaultHome: Boolean,
    val calendarGranted: Boolean,
    /** On API &lt; 33 notifications are not a runtime permission → treated as granted. */
    val notificationsGranted: Boolean,
    /** Spec 017 — NotificationListenerService access (badges); not POST_NOTIFICATIONS. */
    val notificationListenerGranted: Boolean,
    /** True when the platform exposes exact-alarm scheduling checks (API 31+). */
    val exactAlarmApplicable: Boolean,
    /** Meaningful when [exactAlarmApplicable]; otherwise ignored. */
    val exactAlarmGranted: Boolean,
)
