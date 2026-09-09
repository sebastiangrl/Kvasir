package app.kvasir.launcher.domain

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Spec 021 / RF-021-02, RF-021-03 —
 * Pure daily work-completion counts keyed by local `yyyy-MM-dd`.
 */
object PomodoroDailyLog {

    const val RETAIN_DAYS: Int = 30

    /** ISO local date string for [date] (default: today). */
    fun dayKey(date: LocalDate = LocalDate.now()): String = date.toString()

    fun todayCount(map: Map<String, Int>, dayKey: String): Int = map[dayKey] ?: 0

    /** Returns a new map with [dayKey] incremented by one. */
    fun incrementToday(map: Map<String, Int>, dayKey: String): Map<String, Int> {
        val next = map.toMutableMap()
        next[dayKey] = (next[dayKey] ?: 0) + 1
        return next
    }

    /**
     * Drops keys older than [todayKey] − [retainDays], invalid keys, and non-positive counts.
     * Same window style as [HabitHistoryOps.prune].
     */
    fun prune(
        map: Map<String, Int>,
        todayKey: String,
        retainDays: Int = RETAIN_DAYS,
    ): Map<String, Int> {
        val today = parseDayKey(todayKey) ?: return emptyMap()
        val minDay = today.minusDays(retainDays.toLong())
        return map.mapNotNull { (key, count) ->
            if (count <= 0) return@mapNotNull null
            val day = parseDayKey(key) ?: return@mapNotNull null
            if (day.isBefore(minDay)) null else key to count
        }.toMap()
    }

    /** Newest day first; only positive counts. */
    fun sortedEntries(map: Map<String, Int>): List<Pair<String, Int>> =
        map.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.key }
            .map { it.key to it.value }

    private fun parseDayKey(key: String): LocalDate? =
        try {
            LocalDate.parse(key)
        } catch (_: DateTimeParseException) {
            null
        }
}
