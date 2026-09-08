package app.kvasir.launcher.data.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import app.kvasir.launcher.domain.NotificationBadgeOps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Spec 017 / RF-017-01, RF-017-02, RF-017-07 —
 * In-memory set of packages with an active badge-worthy notification.
 * No notification content persisted.
 */
class NotificationBadgeRepository(
    context: Context,
) {
    private val appContext = context.applicationContext

    private val badgedPackagesInternal = MutableStateFlow<Set<String>>(emptySet())
    val badgedPackages: StateFlow<Set<String>> = badgedPackagesInternal.asStateFlow()

    fun clear() {
        badgedPackagesInternal.value = emptySet()
    }

    fun replaceFromActive(active: List<NotificationBadgeOps.ActiveNotif>) {
        badgedPackagesInternal.value = NotificationBadgeOps.packagesWithBadge(active)
    }

    /** Spec 017 / RF-017-02 — whether this app is enabled as a notification listener. */
    fun isListenerEnabled(): Boolean {
        val packageName = appContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val nm = appContext.getSystemService(NotificationManager::class.java) ?: return false
            return nm.isNotificationListenerAccessGranted(
                android.content.ComponentName(
                    appContext,
                    KvasirNotificationListenerService::class.java,
                ),
            )
        }
        val flat = Settings.Secure.getString(
            appContext.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS,
        ) ?: return false
        return flat.split(':').any { component ->
            component.startsWith("$packageName/") || component == packageName
        }
    }

    companion object {
        private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    }
}
