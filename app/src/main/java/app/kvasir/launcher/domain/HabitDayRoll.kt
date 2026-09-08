package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.HabitDayState

/**
 * Spec 005 / RF-005-04 —
 * Pure: if stored day ≠ today, reset completed ids and adopt today.
 */
object HabitDayRoll {

    fun ensureToday(state: HabitDayState?, todayEpochDay: Long): HabitDayState {
        if (state == null || state.epochDay != todayEpochDay) {
            return HabitDayState(epochDay = todayEpochDay, completedIds = emptySet())
        }
        return state
    }
}
