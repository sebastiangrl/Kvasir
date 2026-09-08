package app.kvasir.launcher.ui.home

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Spec 001 / RF-001-06 — pure clock/date formatting (locale-aware; JVM-testable).
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

    fun formatDate(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
        return formatter.format(instant.atZone(zoneId))
    }
}
