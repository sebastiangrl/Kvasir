package app.kvasir.launcher.domain.model

/**
 * Spec 002 / RF-002-01 — launchable app identity + label.
 * Spec 010 — icons are loaded via [app.kvasir.launcher.data.apps.AppIconLoader], not stored here.
 */
data class InstalledApp(
    val packageName: String,
    val activityClassName: String,
    val label: String,
) {
    val componentKey: String
        get() = "$packageName/$activityClassName"
}
