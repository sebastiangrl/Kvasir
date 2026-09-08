package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroNotifyEvent
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Spec 015 / RF-015-05, RF-015-08 — phase transitions and remaining.
 */
class PomodoroPhaseMachineTest {

    private val config = PomodoroConfig(workMinutes = 25, breakMinutes = 5, sessionsPerCycle = 2)
    private val now = 1_000_000L

    @Test
    fun start_beginsWorkSessionOne() {
        val t = PomodoroPhaseMachine.start(config, now)
        assertEquals(PomodoroStatus.Running, t.session.status)
        assertEquals(PomodoroPhase.Work, t.session.phase)
        assertEquals(1, t.session.sessionIndex)
        assertEquals(now + 25 * 60_000L, t.session.endsAtEpochMillis)
        assertNull(t.notify)
    }

    @Test
    fun pauseAndResume_preservesRemaining() {
        val running = PomodoroPhaseMachine.start(config, now).session
        val pausedAt = now + 60_000L
        val paused = PomodoroPhaseMachine.pause(running, pausedAt)
        assertEquals(PomodoroStatus.Paused, paused.session.status)
        assertEquals(24 * 60_000L, paused.session.remainingMillis)
        assertNull(paused.session.endsAtEpochMillis)

        val resumeAt = pausedAt + 10_000L
        val resumed = PomodoroPhaseMachine.resume(paused.session, resumeAt)
        assertEquals(PomodoroStatus.Running, resumed.session.status)
        assertEquals(resumeAt + 24 * 60_000L, resumed.session.endsAtEpochMillis)
        assertNull(resumed.session.remainingMillis)
    }

    @Test
    fun stop_returnsIdle() {
        assertEquals(PomodoroStatus.Idle, PomodoroPhaseMachine.stop().session.status)
    }

    @Test
    fun alarm_workGoesToBreak() {
        val running = PomodoroPhaseMachine.start(config, now).session
        val t = PomodoroPhaseMachine.onAlarmFired(running, config, now + 25 * 60_000L)
        assertEquals(PomodoroPhase.Break, t.session.phase)
        assertEquals(PomodoroStatus.Running, t.session.status)
        assertEquals(1, t.session.sessionIndex)
        assertEquals(PomodoroNotifyEvent.WorkFinished, t.notify)
        assertEquals(now + 25 * 60_000L + 5 * 60_000L, t.session.endsAtEpochMillis)
    }

    @Test
    fun alarm_breakStartsNextWorkWhenSessionsRemain() {
        val afterWork = PomodoroPhaseMachine.onAlarmFired(
            PomodoroPhaseMachine.start(config, now).session,
            config,
            now,
        ).session
        val t = PomodoroPhaseMachine.onAlarmFired(afterWork, config, now + 1)
        assertEquals(PomodoroPhase.Work, t.session.phase)
        assertEquals(2, t.session.sessionIndex)
        assertEquals(PomodoroNotifyEvent.BreakFinished, t.notify)
    }

    @Test
    fun alarm_lastBreakCompletesCycle() {
        // sessionsPerCycle = 2: work1 → break1 → work2 → break2 → idle
        var session = PomodoroPhaseMachine.start(config, now).session
        session = PomodoroPhaseMachine.onAlarmFired(session, config, now).session // break after 1
        session = PomodoroPhaseMachine.onAlarmFired(session, config, now).session // work 2
        session = PomodoroPhaseMachine.onAlarmFired(session, config, now).session // break after 2
        val done = PomodoroPhaseMachine.onAlarmFired(session, config, now)
        assertEquals(PomodoroStatus.Idle, done.session.status)
        assertEquals(PomodoroNotifyEvent.CycleComplete, done.notify)
    }

    @Test
    fun remainingMillis_runningAndPaused() {
        val running = PomodoroPhaseMachine.start(config, now).session
        assertEquals(25 * 60_000L, PomodoroPhaseMachine.remainingMillis(running, now))
        assertEquals(24 * 60_000L, PomodoroPhaseMachine.remainingMillis(running, now + 60_000L))
        val paused = PomodoroPhaseMachine.pause(running, now + 60_000L).session
        assertEquals(24 * 60_000L, PomodoroPhaseMachine.remainingMillis(paused, now + 999_999L))
    }

    @Test
    fun start_clampsInvalidConfig() {
        val wild = PomodoroConfig(workMinutes = 0, breakMinutes = 99, sessionsPerCycle = 100)
        val t = PomodoroPhaseMachine.start(wild, now)
        assertEquals(now + 1 * 60_000L, t.session.endsAtEpochMillis)
    }
}
