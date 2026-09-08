package app.kvasir.launcher.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import app.kvasir.launcher.domain.HabitDayRoll
import app.kvasir.launcher.domain.HabitJson
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HabitDayState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

private const val DATA_STORE_FILE = "kvasir_preferences"

private val Context.kvasirDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_FILE,
)

/**
 * Spec 003 favorites + Spec 005 / RF-005-01, RF-005-03, RF-005-04, RF-005-05, RF-005-08 —
 * Preferences DataStore; Application context only.
 */
class PreferencesRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.kvasirDataStore

    /** Emits the persisted favorite component keys; missing key → empty set (RF-003-09). */
    val favoriteKeys: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] ?: emptySet()
    }

    /** Emits decoded habits; missing/corrupt → empty (RF-005-08). */
    val habits: Flow<List<Habit>> = dataStore.data.map { prefs ->
        HabitJson.decodeHabits(prefs[PreferencesKeys.HABITS_JSON])
    }

    /**
     * Emits day state rolled to today (RF-005-04).
     * Does not write back on read; writes happen on toggle/CRUD.
     */
    val habitDayState: Flow<HabitDayState> = dataStore.data.map { prefs ->
        val decoded = HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON])
        HabitDayRoll.ensureToday(decoded, todayEpochDay())
    }

    suspend fun addFavorite(componentKey: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] ?: emptySet()
            prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] = current + componentKey
        }
    }

    suspend fun removeFavorite(componentKey: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] ?: emptySet()
            prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] = current - componentKey
        }
    }

    suspend fun setFavorite(componentKey: String, favorite: Boolean) {
        if (favorite) {
            addFavorite(componentKey)
        } else {
            removeFavorite(componentKey)
        }
    }

    /** Replaces the set (e.g. prune orphans). Atomic edit. */
    suspend fun setFavoriteKeys(keys: Set<String>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] = keys
        }
    }

    suspend fun setHabits(habits: List<Habit>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.HABITS_JSON] = HabitJson.encodeHabits(habits)
        }
    }

    suspend fun addHabit(label: String): Habit {
        val habit = Habit(id = UUID.randomUUID().toString(), label = label.trim())
        dataStore.edit { prefs ->
            val current = HabitJson.decodeHabits(prefs[PreferencesKeys.HABITS_JSON])
            prefs[PreferencesKeys.HABITS_JSON] = HabitJson.encodeHabits(current + habit)
        }
        return habit
    }

    suspend fun removeHabit(habitId: String) {
        dataStore.edit { prefs ->
            val current = HabitJson.decodeHabits(prefs[PreferencesKeys.HABITS_JSON])
            prefs[PreferencesKeys.HABITS_JSON] =
                HabitJson.encodeHabits(current.filterNot { it.id == habitId })
            // Optional prune of completed id for today.
            val today = todayEpochDay()
            val rolled = HabitDayRoll.ensureToday(
                HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON]),
                today,
            )
            val pruned = rolled.copy(completedIds = rolled.completedIds - habitId)
            prefs[PreferencesKeys.HABIT_DAY_STATE_JSON] = HabitJson.encodeDayState(pruned)
        }
    }

    suspend fun renameHabit(habitId: String, newLabel: String) {
        val trimmed = newLabel.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            val current = HabitJson.decodeHabits(prefs[PreferencesKeys.HABITS_JSON])
            val updated = current.map { habit ->
                if (habit.id == habitId) habit.copy(label = trimmed) else habit
            }
            prefs[PreferencesKeys.HABITS_JSON] = HabitJson.encodeHabits(updated)
        }
    }

    /** RF-005-03, RF-005-04 — toggle completion; roll day atomically before write. */
    suspend fun setHabitCompleted(habitId: String, completed: Boolean) {
        dataStore.edit { prefs ->
            val today = todayEpochDay()
            val rolled = HabitDayRoll.ensureToday(
                HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON]),
                today,
            )
            val ids = if (completed) {
                rolled.completedIds + habitId
            } else {
                rolled.completedIds - habitId
            }
            prefs[PreferencesKeys.HABIT_DAY_STATE_JSON] =
                HabitJson.encodeDayState(rolled.copy(completedIds = ids))
        }
    }

    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}
