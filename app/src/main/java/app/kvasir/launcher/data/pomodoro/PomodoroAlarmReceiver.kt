package app.kvasir.launcher.data.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kvasir.launcher.KvasirApp
import kotlinx.coroutines.launch

/**
 * Spec 015 / RF-015-05, RF-015-08 —
 * Handles phase alarm, stop action, and boot reschedule. No UI.
 */
class PomodoroAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? KvasirApp ?: return
        val controller = app.container.pomodoroController
        val pendingResult = goAsync()
        app.container.applicationScope.launch {
            try {
                when (intent?.action) {
                    ACTION_ALARM -> controller.onAlarmFired()
                    ACTION_STOP -> controller.stop()
                    Intent.ACTION_BOOT_COMPLETED -> controller.rescheduleIfNeeded()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "app.kvasir.launcher.action.POMODORO_ALARM"
        const val ACTION_STOP = "app.kvasir.launcher.action.POMODORO_STOP"
    }
}
