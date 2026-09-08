package app.kvasir.launcher.domain.model

/**
 * Spec 018 / RF-018-01 — App shortcut identity + label (no icon / Bitmap).
 */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
)
