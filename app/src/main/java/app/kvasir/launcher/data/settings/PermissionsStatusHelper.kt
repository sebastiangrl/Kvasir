package app.kvasir.launcher.data.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.notifications.NotificationBadgeRepository

/**
 * Spec 016 / RF-016-04, RF-016-05 + Spec 017 / RF-017-03 —
 * Reads permission / default-Home / listener status with Application context (no Compose).
 */
class PermissionsStatusHelper(
    context: Context,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val notificationBadgeRepository: NotificationBadgeRepository,
) {
    private val appContext = context.applicationContext

    fun snapshot(): PermissionsStatus {
        val calendarGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED

        val notificationsGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        val exactAlarmApplicable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val exactAlarmGranted = if (exactAlarmApplicable) {
            val alarmManager = appContext.getSystemService(AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }

        return PermissionsStatus(
            isDefaultHome = defaultHomeRepository.isDefaultHome(),
            calendarGranted = calendarGranted,
            notificationsGranted = notificationsGranted,
            notificationListenerGranted = notificationBadgeRepository.isListenerEnabled(),
            exactAlarmApplicable = exactAlarmApplicable,
            exactAlarmGranted = exactAlarmGranted,
        )
    }
}
