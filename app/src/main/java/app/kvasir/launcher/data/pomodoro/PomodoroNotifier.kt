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
import app.kvasir.launcher.domain.PomodoroRemainingFormat
import app.kvasir.launcher.domain.model.PomodoroNotifyEvent
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus

/**
 * Spec 015 / RF-015-05, RF-015-06 + Spec 021 / RF-021-07, RF-021-08 —
 * Phase alerts (with vibration) + ongoing lock-screen chronometer. No Compose.
 */
class PomodoroNotifier(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val alerts = NotificationChannel(
            ALERTS_CHANNEL_ID,
            appContext.getString(R.string.pomodoro_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = appContext.getString(R.string.pomodoro_channel_description)
            enableVibration(true)
            vibrationPattern = ALERT_VIBRATION
        }
        val ongoing = NotificationChannel(
            ONGOING_CHANNEL_ID,
            appContext.getString(R.string.pomodoro_ongoing_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = appContext.getString(R.string.pomodoro_ongoing_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(alerts)
        notificationManager.createNotificationChannel(ongoing)
    }

    fun notifyEvent(event: PomodoroNotifyEvent) {
        if (!canPostNotifications()) return
        ensureChannels()
        val title = when (event) {
            PomodoroNotifyEvent.WorkFinished ->
                appContext.getString(R.string.pomodoro_notify_work_done)
            PomodoroNotifyEvent.BreakFinished ->
                appContext.getString(R.string.pomodoro_notify_break_done)
            PomodoroNotifyEvent.CycleComplete ->
                appContext.getString(R.string.pomodoro_notify_cycle_done)
        }
        val body = when (event) {
            PomodoroNotifyEvent.WorkFinished ->
                appContext.getString(R.string.pomodoro_notify_work_body)
            PomodoroNotifyEvent.BreakFinished ->
                appContext.getString(R.string.pomodoro_notify_break_body)
            PomodoroNotifyEvent.CycleComplete ->
                appContext.getString(R.string.pomodoro_notify_cycle_body)
        }
        val notification = NotificationCompat.Builder(appContext, ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openHomePendingIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(ALERT_VIBRATION)
            .addAction(
                0,
                appContext.getString(R.string.pomodoro_stop),
                broadcastPendingIntent(REQUEST_STOP, PomodoroAlarmReceiver.ACTION_STOP),
            )
            .build()
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    /**
     * Spec 021 / RF-021-08 — ongoing timer on lock screen / shade while running or paused.
     */
    fun syncOngoing(session: PomodoroSession) {
        when (session.status) {
            PomodoroStatus.Idle -> cancelOngoing()
            PomodoroStatus.Running, PomodoroStatus.Paused -> {
                if (!canPostNotifications()) {
                    cancelOngoing()
                    return
                }
                ensureChannels()
                val phaseTitle = when {
                    session.status == PomodoroStatus.Paused ->
                        appContext.getString(R.string.pomodoro_phase_paused)
                    session.phase == PomodoroPhase.Work ->
                        appContext.getString(R.string.pomodoro_phase_work)
                    else ->
                        appContext.getString(R.string.pomodoro_phase_break)
                }
                val builder = NotificationCompat.Builder(appContext, ONGOING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(phaseTitle)
                    .setContentIntent(openHomePendingIntent())
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_LOW)

                if (session.status == PomodoroStatus.Running) {
                    val endsAt = session.endsAtEpochMillis
                    if (endsAt != null) {
                        builder
                            .setUsesChronometer(true)
                            .setChronometerCountDown(true)
                            .setWhen(endsAt)
                            .setShowWhen(true)
                            .setContentText(appContext.getString(R.string.pomodoro_ongoing_running_hint))
                    } else {
                        builder.setContentText(
                            PomodoroRemainingFormat.format(0L),
                        )
                    }
                    builder.addAction(
                        0,
                        appContext.getString(R.string.pomodoro_pause),
                        broadcastPendingIntent(REQUEST_PAUSE, PomodoroAlarmReceiver.ACTION_PAUSE),
                    )
                } else {
                    val remaining = session.remainingMillis?.coerceAtLeast(0L) ?: 0L
                    builder
                        .setUsesChronometer(false)
                        .setShowWhen(false)
                        .setContentText(
                            appContext.getString(
                                R.string.pomodoro_ongoing_paused_hint,
                                PomodoroRemainingFormat.format(remaining),
                            ),
                        )
                    builder.addAction(
                        0,
                        appContext.getString(R.string.pomodoro_resume),
                        broadcastPendingIntent(REQUEST_RESUME, PomodoroAlarmReceiver.ACTION_RESUME),
                    )
                }
                builder.addAction(
                    0,
                    appContext.getString(R.string.pomodoro_stop),
                    broadcastPendingIntent(REQUEST_STOP, PomodoroAlarmReceiver.ACTION_STOP),
                )
                notificationManager.notify(ONGOING_NOTIFICATION_ID, builder.build())
            }
        }
    }

    fun cancelPhaseNotification() {
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
        // Legacy 015 id
        notificationManager.cancel(LEGACY_NOTIFICATION_ID)
    }

    fun cancelOngoing() {
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
    }

    fun cancelAllPomodoroNotifications() {
        cancelPhaseNotification()
        cancelOngoing()
    }

    private fun openHomePendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            appContext,
            REQUEST_OPEN,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun broadcastPendingIntent(requestCode: Int, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            requestCode,
            Intent(appContext, PomodoroAlarmReceiver::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ALERTS_CHANNEL_ID = "pomodoro_alerts"
        const val ONGOING_CHANNEL_ID = "pomodoro_ongoing"
        private const val ALERT_NOTIFICATION_ID = 1505
        private const val ONGOING_NOTIFICATION_ID = 1506
        private const val LEGACY_NOTIFICATION_ID = 1502
        private const val REQUEST_OPEN = 1503
        private const val REQUEST_STOP = 1504
        private const val REQUEST_PAUSE = 1507
        private const val REQUEST_RESUME = 1508
        private val ALERT_VIBRATION = longArrayOf(0, 220, 120, 220)
    }
}
