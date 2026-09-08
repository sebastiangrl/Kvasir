package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 014 / RF-014-03, RF-014-05 — streak and 7-day window.
 */
class HabitStreakTest {

    private val today = 20_000L

    @Test
    fun streak_zeroWhenNothingCompleted() {
        assertEquals(0, HabitStreak.streak(emptySet(), todayCompleted = false, todayEpochDay = today))
    }

    @Test
    fun streak_countsTodayAndHistoryBackwards() {
        val history = setOf(today - 1, today - 2)
        assertEquals(
            3,
            HabitStreak.streak(history, todayCompleted = true, todayEpochDay = today),
        )
    }

    @Test
    fun streak_startsFromYesterdayIfTodayIncomplete() {
        val history = setOf(today - 1, today - 2)
        assertEquals(
            2,
            HabitStreak.streak(history, todayCompleted = false, todayEpochDay = today),
        )
    }

    @Test
    fun streak_stopsAtGap() {
        val history = setOf(today - 1, today - 3)
        assertEquals(
            2,
            HabitStreak.streak(history, todayCompleted = true, todayEpochDay = today),
        )
    }

    @Test
    fun lastSevenDays_orderOldestToNewest() {
        val history = setOf(today - 6, today - 1)
        val week = HabitStreak.lastSevenDays(
            historyDays = history,
            todayCompleted = true,
            todayEpochDay = today,
        )
        assertEquals(7, week.size)
        assertTrue(week[0]) // today-6
        assertEquals(listOf(false, false, false, false), week.subList(1, 5))
        assertTrue(week[5]) // today-1
        assertTrue(week[6]) // today
    }
}
