package app.kvasir.launcher.data.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import app.kvasir.launcher.data.apps.AppDetailsNavigator

/**
 * Spec 016 / RF-016-04, RF-016-05 —
 * Opens system settings screens for this app (notifications, exact alarms, details).
 * No Compose; Application context + NEW_TASK.
 */
object AppSettingsNavigator {

    private const val TAG = "AppSettingsNavigator"

    /** JVM-testable package URI (same scheme as [AppDetailsNavigator]). */
    fun packageUriString(packageName: String): String =
        AppDetailsNavigator.packageDetailsUriString(packageName)

    fun appNotificationSettingsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Exact-alarm settings when available (API 31+); otherwise falls back to app details.
     */
    fun exactAlarmSettingsIntent(packageName: String): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse(packageUriString(packageName))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return AppDetailsNavigator.detailsIntent(packageName)
    }

    fun openAppDetails(context: Context, packageName: String) {
        AppDetailsNavigator.open(context, packageName)
    }

    fun openNotificationSettings(context: Context, packageName: String) {
        try {
            context.applicationContext.startActivity(appNotificationSettingsIntent(packageName))
        } catch (t: Throwable) {
            Log.w(TAG, "notification settings failed; falling back to details", t)
            openAppDetails(context, packageName)
        }
    }

    fun openExactAlarmSettings(context: Context, packageName: String) {
        try {
            context.applicationContext.startActivity(exactAlarmSettingsIntent(packageName))
        } catch (t: Throwable) {
            Log.w(TAG, "exact alarm settings failed; falling back to details", t)
            openAppDetails(context, packageName)
        }
    }
}
