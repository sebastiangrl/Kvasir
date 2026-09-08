package app.kvasir.launcher.domain

/**
 * Spec 015 / RF-015-03 — Format remaining time for Home (e.g. `24:59`, `1:05:00`).
 */
object PomodoroRemainingFormat {

    fun format(remainingMillis: Long): String {
        val totalSec = (remainingMillis.coerceAtLeast(0L) / 1000L)
        val hours = totalSec / 3600L
        val minutes = (totalSec % 3600L) / 60L
        val seconds = totalSec % 60L
        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}
