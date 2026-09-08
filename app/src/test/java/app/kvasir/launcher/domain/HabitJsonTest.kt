package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HabitDayState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 005 / RF-005-01, RF-005-08 — JVM codec tests.
 */
class HabitJsonTest {

    @Test
    fun habits_roundTrip() {
        val habits = listOf(
            Habit("a", "Agua"),
            Habit("b", "Caminar"),
        )
        val encoded = HabitJson.encodeHabits(habits)
        assertEquals(habits, HabitJson.decodeHabits(encoded))
    }

    @Test
    fun habits_nullOrBlank_isEmpty() {
        assertTrue(HabitJson.decodeHabits(null).isEmpty())
        assertTrue(HabitJson.decodeHabits("").isEmpty())
        assertTrue(HabitJson.decodeHabits("   ").isEmpty())
    }

    @Test
    fun habits_corrupt_isEmpty() {
        assertTrue(HabitJson.decodeHabits("{not-json").isEmpty())
        assertTrue(HabitJson.decodeHabits("null").isEmpty())
    }

    @Test
    fun dayState_roundTrip() {
        val state = HabitDayState(epochDay = 20_000L, completedIds = setOf("a", "b"))
        val encoded = HabitJson.encodeDayState(state)
        assertEquals(state, HabitJson.decodeDayState(encoded))
    }

    @Test
    fun dayState_nullOrBlank_isNull() {
        assertNull(HabitJson.decodeDayState(null))
        assertNull(HabitJson.decodeDayState(""))
    }

    @Test
    fun dayState_corrupt_isNull() {
        assertNull(HabitJson.decodeDayState("{bad"))
    }

    @Test
    fun history_roundTrip() {
        val history = mapOf(
            "a" to setOf(10L, 11L),
            "b" to setOf(11L),
        )
        val encoded = HabitJson.encodeHistory(history)
        assertEquals(history, HabitJson.decodeHistory(encoded))
    }

    @Test
    fun history_nullOrCorrupt_isEmpty() {
        assertTrue(HabitJson.decodeHistory(null).isEmpty())
        assertTrue(HabitJson.decodeHistory("").isEmpty())
        assertTrue(HabitJson.decodeHistory("{bad").isEmpty())
    }
}
