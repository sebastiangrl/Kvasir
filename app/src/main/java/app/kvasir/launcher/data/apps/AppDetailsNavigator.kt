package app.kvasir.launcher.data.apps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

/**
 * Spec 009 / RF-009-04, RF-009-06 —
 * Opens the system application-details screen for a package (no Compose).
 */
object AppDetailsNavigator {

    private const val TAG = "AppDetailsNavigator"

    /** Pure package URI string — JVM-testable (Android Uri stubs are incomplete). */
    fun packageDetailsUriString(packageName: String): String = "package:$packageName"

    fun detailsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse(packageDetailsUriString(packageName))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun open(context: Context, packageName: String) {
        try {
            context.applicationContext.startActivity(detailsIntent(packageName))
        } catch (t: Throwable) {
            Log.w(TAG, "open details failed for $packageName", t)
        }
    }
}
