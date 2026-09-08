package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HabitDayState
import app.kvasir.launcher.domain.model.HabitHistory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Spec 005 / RF-005-01, RF-005-08 + Spec 014 / RF-014-01 —
 * org.json codec for habits, day state, and history; corrupt input → empty defaults.
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

    /** Spec 014 — `{ "habitId": [epochDay, ...] }`. */
    fun encodeHistory(history: HabitHistory): String {
        val root = JSONObject()
        history.forEach { (habitId, days) ->
            val arr = JSONArray()
            days.sorted().forEach { arr.put(it) }
            root.put(habitId, arr)
        }
        return root.toString()
    }

    fun decodeHistory(raw: String?): HabitHistory {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val root = JSONObject(raw)
            buildMap {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val habitId = keys.next()
                    if (habitId.isNullOrEmpty()) continue
                    val arr = root.optJSONArray(habitId) ?: continue
                    val days = buildSet {
                        for (i in 0 until arr.length()) {
                            if (!arr.isNull(i)) add(arr.getLong(i))
                        }
                    }
                    if (days.isNotEmpty()) put(habitId, days)
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
