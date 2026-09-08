package app.kvasir.launcher.ui.home

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

/**
 * Spec 001 / RF-001-06 — JVM unit tests for pure clock formatting.
 */
class ClockFormatTest {

    private val noonUtc: Instant = Instant.parse("2026-09-08T12:34:56Z")

    @Test
    fun formatTime_containsHourAndMinute_forUsLocale() {
        val text = ClockFormat.formatTime(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue("expected hour in '$text'", text.contains("12"))
        assertTrue("expected minute in '$text'", text.contains("34"))
    }

    @Test
    fun formatDate_containsYear_forUsLocale() {
        val text = ClockFormat.formatDate(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue("expected year in '$text'", text.contains("2026"))
        assertTrue("expected month/day signal in '$text'", text.contains("September") || text.contains("8"))
    }
}
