package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Spec 015 / RF-015-01, RF-015-02 — config/session JSON and clamp.
 */
class PomodoroJsonTest {

    @Test
    fun config_roundTrip() {
        val config = PomodoroConfig(workMinutes = 30, breakMinutes = 10, sessionsPerCycle = 3)
        val decoded = PomodoroJson.decodeConfig(PomodoroJson.encodeConfig(config))
        assertEquals(config, decoded)
    }

    @Test
    fun config_nullOrCorrupt_defaults() {
        assertEquals(PomodoroConfig.Default, PomodoroJson.decodeConfig(null))
        assertEquals(PomodoroConfig.Default, PomodoroJson.decodeConfig("{not json"))
    }

    @Test
    fun config_encodeClamps() {
        val raw = PomodoroJson.encodeConfig(
            PomodoroConfig(workMinutes = 999, breakMinutes = 0, sessionsPerCycle = -1),
        )
        val decoded = PomodoroJson.decodeConfig(raw)
        assertEquals(90, decoded.workMinutes)
        assertEquals(1, decoded.breakMinutes)
        assertEquals(1, decoded.sessionsPerCycle)
    }

    @Test
    fun session_roundTripRunning() {
        val session = PomodoroSession(
            status = PomodoroStatus.Running,
            phase = PomodoroPhase.Break,
            sessionIndex = 2,
            endsAtEpochMillis = 42L,
            remainingMillis = null,
        )
        val decoded = PomodoroJson.decodeSession(PomodoroJson.encodeSession(session))
        assertEquals(session, decoded)
    }

    @Test
    fun session_roundTripPaused() {
        val session = PomodoroSession(
            status = PomodoroStatus.Paused,
            phase = PomodoroPhase.Work,
            sessionIndex = 1,
            endsAtEpochMillis = null,
            remainingMillis = 12_000L,
        )
        assertEquals(session, PomodoroJson.decodeSession(PomodoroJson.encodeSession(session)))
    }

    @Test
    fun session_nullOrCorrupt_idle() {
        assertEquals(PomodoroSession.Idle, PomodoroJson.decodeSession(null))
        assertEquals(PomodoroSession.Idle, PomodoroJson.decodeSession("[]"))
    }
}
