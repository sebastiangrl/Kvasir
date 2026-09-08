package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.HabitDayState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 005 / RF-005-04 — JVM day-roll tests.
 */
class HabitDayRollTest {

    @Test
    fun ensureToday_null_returnsEmptyToday() {
        val result = HabitDayRoll.ensureToday(null, todayEpochDay = 100L)
        assertEquals(100L, result.epochDay)
        assertTrue(result.completedIds.isEmpty())
    }

    @Test
    fun ensureToday_sameDay_keepsCompleted() {
        val state = HabitDayState(epochDay = 100L, completedIds = setOf("a"))
        val result = HabitDayRoll.ensureToday(state, todayEpochDay = 100L)
        assertEquals(state, result)
    }

    @Test
    fun ensureToday_differentDay_resetsCompleted() {
        val state = HabitDayState(epochDay = 99L, completedIds = setOf("a", "b"))
        val result = HabitDayRoll.ensureToday(state, todayEpochDay = 100L)
        assertEquals(100L, result.epochDay)
        assertTrue(result.completedIds.isEmpty())
    }
}
