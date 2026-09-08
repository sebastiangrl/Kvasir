package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.HabitHistory

/**
 * Spec 014 / RF-014-02, RF-014-06, RF-014-07 —
 * Pure history mutations: archive a day, prune, remove habit.
 */
object HabitHistoryOps {

    const val RETAIN_DAYS: Int = 90

    /** Adds [epochDay] to each id in [completedIds]; returns a new map. */
    fun archiveDay(
        history: HabitHistory,
        epochDay: Long,
        completedIds: Set<String>,
    ): HabitHistory {
        if (completedIds.isEmpty()) return history
        val mutable = history.mapValues { it.value.toMutableSet() }.toMutableMap()
        completedIds.forEach { id ->
            val days = mutable.getOrPut(id) { mutableSetOf() }
            days.add(epochDay)
        }
        return mutable.mapValues { it.value.toSet() }
    }

    /** Drops epochDays older than [todayEpochDay] − [retainDays]. */
    fun prune(
        history: HabitHistory,
        todayEpochDay: Long,
        retainDays: Int = RETAIN_DAYS,
    ): HabitHistory {
        val minDay = todayEpochDay - retainDays
        return history.mapValues { (_, days) ->
            days.filter { it >= minDay }.toSet()
        }.filterValues { it.isNotEmpty() }
    }

    fun removeHabit(history: HabitHistory, habitId: String): HabitHistory =
        history - habitId
}
