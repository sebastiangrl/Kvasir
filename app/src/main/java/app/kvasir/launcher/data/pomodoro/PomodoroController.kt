package app.kvasir.launcher.data.pomodoro

import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.domain.PomodoroDailyLog
import app.kvasir.launcher.domain.PomodoroPhaseMachine
import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroNotifyEvent
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus

/**
 * Spec 015 / RF-015-01, RF-015-02, RF-015-05, RF-015-08 +
 * Spec 021 / RF-021-03, RF-021-07, RF-021-08 —
 * Orchestrates prefs + alarm + notify + daily work count + ongoing. No Compose.
 */
class PomodoroController(
    private val preferencesRepository: PreferencesRepository,
    private val scheduler: PomodoroAlarmScheduler,
    private val notifier: PomodoroNotifier,
) {

    suspend fun start(nowEpochMillis: Long = System.currentTimeMillis()) {
        val config = preferencesRepository.getPomodoroConfig()
        val transition = PomodoroPhaseMachine.start(config, nowEpochMillis)
        persistAndSync(transition.session)
    }

    suspend fun pause(nowEpochMillis: Long = System.currentTimeMillis()) {
        val session = preferencesRepository.getPomodoroSession()
        val transition = PomodoroPhaseMachine.pause(session, nowEpochMillis)
        persistAndSync(transition.session)
    }

    suspend fun resume(nowEpochMillis: Long = System.currentTimeMillis()) {
        val session = preferencesRepository.getPomodoroSession()
        val transition = PomodoroPhaseMachine.resume(session, nowEpochMillis)
        persistAndSync(transition.session)
    }

    suspend fun stop() {
        scheduler.cancel()
        preferencesRepository.setPomodoroSession(PomodoroPhaseMachine.stop().session)
        notifier.cancelAllPomodoroNotifications()
    }

    /** Alarm fired: advance phase, notify, count completed work, reschedule if still running. */
    suspend fun onAlarmFired(nowEpochMillis: Long = System.currentTimeMillis()) {
        val config = preferencesRepository.getPomodoroConfig()
        val session = preferencesRepository.getPomodoroSession()
        val transition = PomodoroPhaseMachine.onAlarmFired(session, config, nowEpochMillis)
        persistAndSync(transition.session)
        if (transition.notify == PomodoroNotifyEvent.WorkFinished) {
            preferencesRepository.incrementPomodoroWorkCompleted(PomodoroDailyLog.dayKey())
        }
        transition.notify?.let { notifier.notifyEvent(it) }
    }

    /**
     * After boot or process start: if session is Running with a future endsAt, reschedule;
     * if endsAt already passed, treat as alarm fired; sync ongoing for paused/running.
     */
    suspend fun rescheduleIfNeeded(nowEpochMillis: Long = System.currentTimeMillis()) {
        val session = preferencesRepository.getPomodoroSession()
        when (session.status) {
            PomodoroStatus.Idle -> {
                scheduler.cancel()
                notifier.cancelOngoing()
            }
            PomodoroStatus.Paused -> {
                scheduler.cancel()
                notifier.syncOngoing(session)
            }
            PomodoroStatus.Running -> {
                val endsAt = session.endsAtEpochMillis
                if (endsAt == null) {
                    scheduler.cancel()
                    notifier.cancelOngoing()
                } else if (endsAt <= nowEpochMillis) {
                    onAlarmFired(nowEpochMillis)
                } else {
                    scheduler.schedule(endsAt)
                    notifier.syncOngoing(session)
                }
            }
        }
    }

    suspend fun updateConfig(config: PomodoroConfig) {
        preferencesRepository.setPomodoroConfig(config.clamped())
    }

    private suspend fun persistAndSync(session: PomodoroSession) {
        preferencesRepository.setPomodoroSession(session)
        when (session.status) {
            PomodoroStatus.Running -> {
                val endsAt = session.endsAtEpochMillis
                if (endsAt != null) {
                    scheduler.schedule(endsAt)
                } else {
                    scheduler.cancel()
                }
            }
            PomodoroStatus.Idle, PomodoroStatus.Paused -> scheduler.cancel()
        }
        notifier.syncOngoing(session)
    }
}
