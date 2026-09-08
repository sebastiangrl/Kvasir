package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HabitDayState
import org.json.JSONArray
import org.json.JSONObject

/**
 * Spec 005 / RF-005-01, RF-005-08 —
 * org.json codec for habits and day state; corrupt input → empty defaults.
 */
object HabitJson {

    fun encodeHabits(habits: List<Habit>): String {
        val array = JSONArray()
        habits.forEach { habit ->
            array.put(
                JSONObject()
                    .put("id", habit.id)
                    .put("label", habit.label),
            )
        }
        return array.toString()
    }

    fun decodeHabits(raw: String?): List<Habit> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", "")
                    val label = obj.optString("label", "")
                    if (id.isNotEmpty()) {
                        add(Habit(id = id, label = label))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encodeDayState(state: HabitDayState): String {
        val ids = JSONArray()
        state.completedIds.forEach { ids.put(it) }
        return JSONObject()
            .put("epochDay", state.epochDay)
            .put("completedIds", ids)
            .toString()
    }

    fun decodeDayState(raw: String?): HabitDayState? {
        if (raw.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(raw)
            val epochDay = obj.getLong("epochDay")
            val idsArray = obj.optJSONArray("completedIds") ?: JSONArray()
            val ids = buildSet {
                for (i in 0 until idsArray.length()) {
                    val id = idsArray.optString(i, "")
                    if (id.isNotEmpty()) add(id)
                }
            }
            HabitDayState(epochDay = epochDay, completedIds = ids)
        } catch (_: Exception) {
            null
        }
    }
}
