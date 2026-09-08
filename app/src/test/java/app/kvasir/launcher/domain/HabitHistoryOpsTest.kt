package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 014 / RF-014-02, RF-014-06, RF-014-07 — history mutations.
 */
class HabitHistoryOpsTest {

    @Test
    fun archiveDay_addsCompletedIds() {
        val next = HabitHistoryOps.archiveDay(
            history = mapOf("a" to setOf(100L)),
            epochDay = 101L,
            completedIds = setOf("a", "b"),
        )
        assertEquals(setOf(100L, 101L), next["a"])
        assertEquals(setOf(101L), next["b"])
    }

    @Test
    fun archiveDay_emptyCompleted_noop() {
        val history = mapOf("a" to setOf(100L))
        assertEquals(history, HabitHistoryOps.archiveDay(history, 101L, emptySet()))
    }

    @Test
    fun prune_dropsOlderThanRetainWindow() {
        val today = 1_000L
        val history = mapOf(
            "a" to setOf(today, today - 50, today - 91, today - 200),
        )
        val pruned = HabitHistoryOps.prune(history, todayEpochDay = today, retainDays = 90)
        assertEquals(setOf(today, today - 50), pruned["a"])
    }

    @Test
    fun removeHabit_dropsKey() {
        val history = mapOf("a" to setOf(1L), "b" to setOf(2L))
        assertEquals(mapOf("b" to setOf(2L)), HabitHistoryOps.removeHabit(history, "a"))
    }
}
