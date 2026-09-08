package app.kvasir.launcher.data.pomodoro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Spec 015 / RF-015-05 — schedule / cancel exact end-of-phase alarms (Application context).
 */
class PomodoroAlarmScheduler(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val alarmManager =
        appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(endsAtEpochMillis: Long) {
        val pi = alarmPendingIntent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endsAtEpochMillis,
                    pi,
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, endsAtEpochMillis, pi)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm schedule denied", e)
        }
    }

    fun cancel() {
        alarmManager.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(appContext, PomodoroAlarmReceiver::class.java).apply {
            action = PomodoroAlarmReceiver.ACTION_ALARM
        }
        return PendingIntent.getBroadcast(
            appContext,
            REQUEST_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "PomodoroAlarm"
        private const val REQUEST_ALARM = 1501
    }
}
