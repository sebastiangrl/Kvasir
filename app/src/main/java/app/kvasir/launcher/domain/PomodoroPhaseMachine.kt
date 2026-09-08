package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroNotifyEvent
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus

/**
 * Spec 015 / RF-015-05, RF-015-08 —
 * Pure phase machine: start / pause / resume / stop / alarm; no Android APIs.
 */
object PomodoroPhaseMachine {

    data class Transition(
        val session: PomodoroSession,
        val notify: PomodoroNotifyEvent? = null,
    )

    fun start(config: PomodoroConfig, nowEpochMillis: Long): Transition {
        val c = config.clamped()
        return Transition(
            session = PomodoroSession(
                status = PomodoroStatus.Running,
                phase = PomodoroPhase.Work,
                sessionIndex = 1,
                endsAtEpochMillis = nowEpochMillis + c.workMinutes * MINUTE_MS,
                remainingMillis = null,
            ),
        )
    }

    fun pause(session: PomodoroSession, nowEpochMillis: Long): Transition {
        if (session.status != PomodoroStatus.Running) {
            return Transition(session)
        }
        val endsAt = session.endsAtEpochMillis ?: return Transition(session)
        val remaining = (endsAt - nowEpochMillis).coerceAtLeast(0L)
        return Transition(
            session = session.copy(
                status = PomodoroStatus.Paused,
                endsAtEpochMillis = null,
                remainingMillis = remaining,
            ),
        )
    }

    fun resume(session: PomodoroSession, nowEpochMillis: Long): Transition {
        if (session.status != PomodoroStatus.Paused) {
            return Transition(session)
        }
        val remaining = session.remainingMillis ?: return Transition(session)
        return Transition(
            session = session.copy(
                status = PomodoroStatus.Running,
                endsAtEpochMillis = nowEpochMillis + remaining.coerceAtLeast(0L),
                remainingMillis = null,
            ),
        )
    }

    fun stop(): Transition = Transition(session = PomodoroSession.Idle)

    /**
     * Alarm fired at end of current running phase.
     * Work → Break (auto); Break → next Work or Idle + [PomodoroNotifyEvent.CycleComplete].
     */
    fun onAlarmFired(
        session: PomodoroSession,
        config: PomodoroConfig,
        nowEpochMillis: Long,
    ): Transition {
        if (session.status != PomodoroStatus.Running) {
            return Transition(PomodoroSession.Idle)
        }
        val c = config.clamped()
        return when (session.phase) {
            PomodoroPhase.Work -> Transition(
                session = PomodoroSession(
                    status = PomodoroStatus.Running,
                    phase = PomodoroPhase.Break,
                    sessionIndex = session.sessionIndex,
                    endsAtEpochMillis = nowEpochMillis + c.breakMinutes * MINUTE_MS,
                    remainingMillis = null,
                ),
                notify = PomodoroNotifyEvent.WorkFinished,
            )
            PomodoroPhase.Break -> {
                if (session.sessionIndex >= c.sessionsPerCycle) {
                    Transition(
                        session = PomodoroSession.Idle,
                        notify = PomodoroNotifyEvent.CycleComplete,
                    )
                } else {
                    val nextIndex = session.sessionIndex + 1
                    Transition(
                        session = PomodoroSession(
                            status = PomodoroStatus.Running,
                            phase = PomodoroPhase.Work,
                            sessionIndex = nextIndex,
                            endsAtEpochMillis = nowEpochMillis + c.workMinutes * MINUTE_MS,
                            remainingMillis = null,
                        ),
                        notify = PomodoroNotifyEvent.BreakFinished,
                    )
                }
            }
        }
    }

    fun remainingMillis(session: PomodoroSession, nowEpochMillis: Long): Long =
        when (session.status) {
            PomodoroStatus.Idle -> 0L
            PomodoroStatus.Paused -> session.remainingMillis?.coerceAtLeast(0L) ?: 0L
            PomodoroStatus.Running -> {
                val endsAt = session.endsAtEpochMillis ?: return 0L
                (endsAt - nowEpochMillis).coerceAtLeast(0L)
            }
        }

    private const val MINUTE_MS = 60_000L
}
