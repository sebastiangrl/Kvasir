package app.kvasir.launcher.data.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import app.kvasir.launcher.KvasirApp
import app.kvasir.launcher.domain.NotificationBadgeOps

/**
 * Spec 017 / RF-017-01, RF-017-07 —
 * Rescans active notifications into [NotificationBadgeRepository]; no content stored.
 */
class KvasirNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        rescan()
    }

    override fun onListenerDisconnected() {
        badgeRepositoryOrNull()?.clear()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        rescan()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        rescan()
    }

    private fun rescan() {
        val repo = badgeRepositoryOrNull() ?: return
        try {
            val active = activeNotifications
                ?.map { toActiveNotif(it) }
                .orEmpty()
            repo.replaceFromActive(active)
        } catch (t: Throwable) {
            Log.w(TAG, "rescan failed; clearing badges", t)
            repo.clear()
        }
    }

    private fun badgeRepositoryOrNull(): NotificationBadgeRepository? {
        val app = applicationContext as? KvasirApp ?: return null
        return app.container.notificationBadgeRepository
    }

    companion object {
        private const val TAG = "KvasirNotifListener"

        fun toActiveNotif(sbn: StatusBarNotification): NotificationBadgeOps.ActiveNotif {
            val flags = sbn.notification?.flags ?: 0
            val ongoing = flags and Notification.FLAG_ONGOING_EVENT != 0
            val summary = flags and Notification.FLAG_GROUP_SUMMARY != 0
            return NotificationBadgeOps.ActiveNotif(
                packageName = sbn.packageName.orEmpty(),
                isOngoing = ongoing,
                isGroupSummary = summary,
            )
        }
    }
}
