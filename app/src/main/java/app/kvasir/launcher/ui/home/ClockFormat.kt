package app.kvasir.launcher.ui.home

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Spec 001 / RF-001-06 + Spec 011 / RF-011-01…03 —
 * Pure clock/date formatting (locale-aware; JVM-testable).
 */
object ClockFormat {
    fun formatTime(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale)
        return formatter.format(instant.atZone(zoneId))
    }

    /**
     * Spec 011 / RF-011-01, RF-011-03 — short weekday (e.g. MAR / TUE), uppercased.
     */
    fun formatWeekdayAbbreviated(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val zoned = instant.atZone(zoneId)
        val short = DateTimeFormatter.ofPattern("EEE", locale).format(zoned).trim()
        val raw = if (short.isNotEmpty()) {
            short
        } else {
            DateTimeFormatter.ofPattern("EEEE", locale).format(zoned).trim().take(3)
        }
        return raw.uppercase(locale)
    }

    /**
     * Spec 011 / RF-011-02, RF-011-03 — short date (day + month), not FormatStyle.FULL.
     */
    fun formatDate(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
        return formatter.format(instant.atZone(zoneId))
    }
}
