package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.NextCalendarEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Spec 012 / RF-012-03 — pure “title · time/date” line for Home (JVM-testable).
 */
object NextEventLineFormat {

    fun format(
        event: NextCalendarEvent,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val title = event.title.trim()
        if (title.isEmpty()) return ""

        val whenText = if (event.allDay) {
            DateTimeFormatter.ofPattern("d MMM", locale)
                .format(Instant.ofEpochMilli(event.beginEpochMillis).atZone(zoneId))
        } else {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale)
                .format(Instant.ofEpochMilli(event.beginEpochMillis).atZone(zoneId))
        }
        return "$title · $whenText"
    }
}
