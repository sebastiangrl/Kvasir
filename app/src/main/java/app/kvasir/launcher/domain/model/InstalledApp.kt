package app.kvasir.launcher.domain.model

/**
 * Spec 002 / RF-002-01 — launchable app without icons/Bitmaps.
 * [componentKey] is the stable identity for favorites (003) and UI keys.
 */
data class InstalledApp(
    val packageName: String,
    val activityClassName: String,
    val label: String,
) {
    val componentKey: String
        get() = "$packageName/$activityClassName"
}
