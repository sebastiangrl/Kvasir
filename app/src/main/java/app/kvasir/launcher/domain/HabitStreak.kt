package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.HabitHistory

/**
 * Spec 014 / RF-014-03, RF-014-05 —
 * Pure streak and last-7-days views (history + today's completion).
 */
object HabitStreak {

    /**
     * Consecutive completed days ending at today (if [todayCompleted]) or yesterday.
     * Uses [historyDays] plus today when completed.
     */
    fun streak(
        historyDays: Set<Long>,
        todayCompleted: Boolean,
        todayEpochDay: Long,
    ): Int {
        var day = if (todayCompleted) todayEpochDay else todayEpochDay - 1L
        if (!isCompleted(day, historyDays, todayCompleted, todayEpochDay)) {
            return 0
        }
        var count = 0
        while (isCompleted(day, historyDays, todayCompleted, todayEpochDay)) {
            count++
            day--
        }
        return count
    }

    fun streakForHabit(
        history: HabitHistory,
        habitId: String,
        todayCompleted: Boolean,
        todayEpochDay: Long,
    ): Int = streak(
        historyDays = history[habitId].orEmpty(),
        todayCompleted = todayCompleted,
        todayEpochDay = todayEpochDay,
    )

    /**
     * Seven booleans: index 0 = [todayEpochDay] − 6 … index 6 = today.
     * Today uses [todayCompleted]; older days use [historyDays].
     */
    fun lastSevenDays(
        historyDays: Set<Long>,
        todayCompleted: Boolean,
        todayEpochDay: Long,
    ): List<Boolean> {
        return (6 downTo 0).map { offset ->
            val day = todayEpochDay - offset
            isCompleted(day, historyDays, todayCompleted, todayEpochDay)
        }
    }

    private fun isCompleted(
        day: Long,
        historyDays: Set<Long>,
        todayCompleted: Boolean,
        todayEpochDay: Long,
    ): Boolean {
        return if (day == todayEpochDay) {
            todayCompleted
        } else {
            day in historyDays
        }
    }
}
