package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Spec 021 / RF-021-02, RF-021-03 — daily map ops + JSON.
 */
class PomodoroDailyLogTest {

    private val today = LocalDate.of(2026, 9, 9)
    private val todayKey = PomodoroDailyLog.dayKey(today)

    @Test
    fun dayKey_isoLocalDate() {
        assertEquals("2026-09-09", todayKey)
    }

    @Test
    fun todayCount_missingIsZero() {
        assertEquals(0, PomodoroDailyLog.todayCount(emptyMap(), todayKey))
    }

    @Test
    fun incrementToday_addsOne() {
        val once = PomodoroDailyLog.incrementToday(emptyMap(), todayKey)
        assertEquals(1, PomodoroDailyLog.todayCount(once, todayKey))
        val twice = PomodoroDailyLog.incrementToday(once, todayKey)
        assertEquals(2, PomodoroDailyLog.todayCount(twice, todayKey))
    }

    @Test
    fun prune_dropsOlderThanRetainWindow() {
        val oldKey = PomodoroDailyLog.dayKey(today.minusDays(31))
        val edgeKey = PomodoroDailyLog.dayKey(today.minusDays(30))
        val map = mapOf(
            todayKey to 2,
            edgeKey to 1,
            oldKey to 9,
        )
        val pruned = PomodoroDailyLog.prune(map, todayKey)
        assertEquals(2, pruned[todayKey])
        assertEquals(1, pruned[edgeKey])
        assertTrue(oldKey !in pruned)
    }

    @Test
    fun prune_dropsInvalidAndNonPositive() {
        val yesterday = PomodoroDailyLog.dayKey(today.minusDays(1))
        val pruned = PomodoroDailyLog.prune(
            mapOf(
                todayKey to 0,
                "not-a-date" to 3,
                yesterday to 2,
            ),
            todayKey,
        )
        assertTrue(todayKey !in pruned)
        assertTrue("not-a-date" !in pruned)
        assertEquals(2, pruned[yesterday])
    }

    @Test
    fun sortedEntries_newestFirst() {
        val d1 = PomodoroDailyLog.dayKey(today.minusDays(1))
        val d2 = PomodoroDailyLog.dayKey(today.minusDays(2))
        val entries = PomodoroDailyLog.sortedEntries(
            mapOf(d2 to 1, todayKey to 3, d1 to 2),
        )
        assertEquals(listOf(todayKey to 3, d1 to 2, d2 to 1), entries)
    }

    @Test
    fun json_roundTrip() {
        val map = mapOf(todayKey to 4, PomodoroDailyLog.dayKey(today.minusDays(1)) to 1)
        val decoded = PomodoroJson.decodeDaily(PomodoroJson.encodeDaily(map))
        assertEquals(map, decoded)
    }

    @Test
    fun json_nullOrCorrupt_empty() {
        assertEquals(emptyMap<String, Int>(), PomodoroJson.decodeDaily(null))
        assertEquals(emptyMap<String, Int>(), PomodoroJson.decodeDaily("{bad"))
        assertEquals(emptyMap<String, Int>(), PomodoroJson.decodeDaily("[]"))
    }
}
