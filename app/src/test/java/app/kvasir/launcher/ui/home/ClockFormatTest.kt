package app.kvasir.launcher.ui.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

/**
 * Spec 001 / RF-001-06 + Spec 011 / RF-011-01…03 + Spec 019 / RF-019-06 —
 * JVM unit tests for clock formatting.
 */
class ClockFormatTest {

    /** Tuesday, 8 Sep 2026 12:34:56 UTC */
    private val noonUtc: Instant = Instant.parse("2026-09-08T12:34:56Z")

    private val esEs: Locale = Locale.forLanguageTag("es-ES")

    @Test
    fun formatTime_short_containsHourAndMinute_noSeconds_forUsLocale() {
        val text = ClockFormat.formatTime(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue("expected hour in '$text'", text.contains("12"))
        assertTrue("expected minute in '$text'", text.contains("34"))
        assertFalse("expected no seconds in SHORT time '$text'", text.contains("56"))
    }

    @Test
    fun formatWeekdayAbbreviated_us_isTuesdayUppercase() {
        val text = ClockFormat.formatWeekdayAbbreviated(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue("expected TUE in '$text'", text.contains("TUE"))
        assertTrue("expected uppercase '$text'", text == text.uppercase(Locale.US))
    }

    @Test
    fun formatWeekdayAbbreviated_es_isMartesUppercase() {
        val text = ClockFormat.formatWeekdayAbbreviated(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = esEs,
        )
        assertTrue("expected MAR in '$text'", text.contains("MAR"))
        assertTrue("expected uppercase '$text'", text == text.uppercase(esEs))
    }

    @Test
    fun formatDate_short_us_isMonthDayUppercase_withoutYear() {
        val text = ClockFormat.formatDate(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue("expected day in '$text'", text.contains("8"))
        assertTrue("expected SEP in '$text'", text.contains("SEP"))
        assertTrue("expected uppercase '$text'", text == text.uppercase(Locale.US))
        assertFalse("expected no year in short date '$text'", text.contains("2026"))
    }

    @Test
    fun formatDate_short_es_hasMonthAndDay_withoutYear() {
        val text = ClockFormat.formatDate(
            instant = noonUtc,
            zoneId = ZoneOffset.UTC,
            locale = esEs,
        )
        assertTrue("expected day in '$text'", text.contains("8"))
        assertTrue(
            "expected sep signal in '$text'",
            text.contains("SEP", ignoreCase = true) || text.contains("SEPT", ignoreCase = true),
        )
        assertTrue("expected uppercase '$text'", text == text.uppercase(esEs))
        assertFalse("expected no year in short date '$text'", text.contains("2026"))
    }
}
