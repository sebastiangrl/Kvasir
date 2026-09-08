package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Spec 015 / RF-015-03 — remaining display format.
 */
class PomodoroRemainingFormatTest {

    @Test
    fun format_minutesAndSeconds() {
        assertEquals("24:59", PomodoroRemainingFormat.format(24 * 60_000L + 59_000L))
        assertEquals("0:00", PomodoroRemainingFormat.format(0L))
        assertEquals("0:00", PomodoroRemainingFormat.format(-5L))
    }

    @Test
    fun format_hoursWhenNeeded() {
        assertEquals("1:05:00", PomodoroRemainingFormat.format(65 * 60_000L))
    }
}
