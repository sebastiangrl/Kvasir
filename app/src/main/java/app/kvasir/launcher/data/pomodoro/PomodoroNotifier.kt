package app.kvasir.launcher.data.pomodoro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.kvasir.launcher.MainActivity
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.model.PomodoroNotifyEvent

/**
 * Spec 015 / RF-015-05, RF-015-06 —
 * Phase/cycle notifications; no-op if POST_NOTIFICATIONS denied.
 */
class PomodoroNotifier(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            appContext.getString(R.string.pomodoro_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = appContext.getString(R.string.pomodoro_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun notifyEvent(event: PomodoroNotifyEvent) {
        if (!canPostNotifications()) return
        ensureChannel()
        val title = when (event) {
            PomodoroNotifyEvent.WorkFinished ->
                appContext.getString(R.string.pomodoro_notify_work_done)
            PomodoroNotifyEvent.BreakFinished ->
                appContext.getString(R.string.pomodoro_notify_break_done)
            PomodoroNotifyEvent.CycleComplete ->
                appContext.getString(R.string.pomodoro_notify_cycle_done)
        }
        val contentIntent = PendingIntent.getActivity(
            appContext,
            REQUEST_OPEN,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_STOP,
            Intent(appContext, PomodoroAlarmReceiver::class.java).apply {
                action = PomodoroAlarmReceiver.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(appContext.getString(R.string.pomodoro_notify_body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                appContext.getString(R.string.pomodoro_stop),
                stopIntent,
            )
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /** Spec 015 / RF-015-08 — clear phase notification so stop does not leave a stale alert. */
    fun cancelPhaseNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CHANNEL_ID = "pomodoro"
        private const val NOTIFICATION_ID = 1502
        private const val REQUEST_OPEN = 1503
        private const val REQUEST_STOP = 1504
    }
}
