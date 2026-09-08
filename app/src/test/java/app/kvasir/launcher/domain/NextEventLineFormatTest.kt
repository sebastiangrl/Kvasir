package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.NextCalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.util.Locale

/**
 * Spec 012 / RF-012-03 — JVM tests for next-event Home line formatting.
 */
class NextEventLineFormatTest {

    private val begin = java.time.Instant.parse("2026-09-08T19:30:00Z").toEpochMilli()

    @Test
    fun format_timed_us_containsTitleAndTime() {
        val line = NextEventLineFormat.format(
            event = NextCalendarEvent(
                title = "Weekly Mezzy",
                beginEpochMillis = begin,
                allDay = false,
                eventId = 1L,
            ),
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertTrue(line.startsWith("Weekly Mezzy · "))
        assertTrue("expected time in '$line'", line.contains("7:30") || line.contains("19:30"))
    }

    @Test
    fun format_allDay_usesShortDate_notClock() {
        val line = NextEventLineFormat.format(
            event = NextCalendarEvent(
                title = "Holiday",
                beginEpochMillis = begin,
                allDay = true,
                eventId = 2L,
            ),
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertEquals("Holiday · 8 Sep", line)
    }

    @Test
    fun format_blankTitle_returnsEmpty() {
        val line = NextEventLineFormat.format(
            event = NextCalendarEvent(
                title = "   ",
                beginEpochMillis = begin,
                allDay = false,
                eventId = null,
            ),
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )
        assertEquals("", line)
    }

    @Test
    fun format_timed_es_containsTitle() {
        val line = NextEventLineFormat.format(
            event = NextCalendarEvent(
                title = "Reunión",
                beginEpochMillis = begin,
                allDay = false,
                eventId = 3L,
            ),
            zoneId = ZoneOffset.UTC,
            locale = Locale.forLanguageTag("es-ES"),
        )
        assertTrue(line.startsWith("Reunión · "))
    }
}
