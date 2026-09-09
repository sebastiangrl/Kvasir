package app.kvasir.launcher.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import app.kvasir.launcher.domain.HabitDayRoll
import app.kvasir.launcher.domain.HabitHistoryOps
import app.kvasir.launcher.domain.HabitJson
import app.kvasir.launcher.domain.PomodoroDailyLog
import app.kvasir.launcher.domain.PomodoroJson
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HabitDayState
import app.kvasir.launcher.domain.model.HabitHistory
import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

private const val DATA_STORE_FILE = "kvasir_preferences"

private val Context.kvasirDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_FILE,
)

/**
 * Spec 003 + Spec 005 + Spec 006 + Spec 014 / RF-014-01, RF-014-02, RF-014-07 +
 * Spec 015 / RF-015-01, RF-015-02 + Spec 021 / RF-021-02 —
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
     * Disk roll + history archive happen via [ensureHabitDayRolled] / writes.
     */
    val habitDayState: Flow<HabitDayState> = dataStore.data.map { prefs ->
        val decoded = HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON])
        HabitDayRoll.ensureToday(decoded, todayEpochDay())
    }

    /** Spec 014 — completed epochDays per habit; missing/corrupt → empty. */
    val habitHistory: Flow<HabitHistory> = dataStore.data.map { prefs ->
        HabitJson.decodeHistory(prefs[PreferencesKeys.HABIT_HISTORY_JSON])
    }

    /** Emits theme mode; missing/invalid → Light (RF-006-06). */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStorage(prefs[PreferencesKeys.THEME_MODE])
    }

    /** Spec 015 — Pomodoro config; missing/corrupt → defaults 25/5/4. */
    val pomodoroConfig: Flow<PomodoroConfig> = dataStore.data.map { prefs ->
        PomodoroJson.decodeConfig(prefs[PreferencesKeys.POMODORO_CONFIG_JSON])
    }

    /** Spec 015 — active session; missing/corrupt → idle. */
    val pomodoroSession: Flow<PomodoroSession> = dataStore.data.map { prefs ->
        PomodoroJson.decodeSession(prefs[PreferencesKeys.POMODORO_SESSION_JSON])
    }

    /** Spec 021 / RF-021-02 — completed work sessions by local day; missing/corrupt → empty. */
    val pomodoroDaily: Flow<Map<String, Int>> = dataStore.data.map { prefs ->
        PomodoroJson.decodeDaily(prefs[PreferencesKeys.POMODORO_DAILY_JSON])
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
            val today = todayEpochDay()
            val rolled = archivePreviousDayIfNeeded(prefs, today)
            val current = HabitJson.decodeHabits(prefs[PreferencesKeys.HABITS_JSON])
            prefs[PreferencesKeys.HABITS_JSON] =
                HabitJson.encodeHabits(current.filterNot { it.id == habitId })
            val history = HabitHistoryOps.removeHabit(readHistory(prefs), habitId)
            prefs[PreferencesKeys.HABIT_HISTORY_JSON] = HabitJson.encodeHistory(history)
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

    /**
     * RF-005-03, RF-005-04 + Spec 014 / RF-014-02 —
     * Toggle completion; archive previous day into history when rolling.
     */
    suspend fun setHabitCompleted(habitId: String, completed: Boolean) {
        dataStore.edit { prefs ->
            val today = todayEpochDay()
            val rolled = archivePreviousDayIfNeeded(prefs, today)
            val ids = if (completed) {
                rolled.completedIds + habitId
            } else {
                rolled.completedIds - habitId
            }
            prefs[PreferencesKeys.HABIT_DAY_STATE_JSON] =
                HabitJson.encodeDayState(rolled.copy(completedIds = ids))
        }
    }

    /**
     * Spec 014 / RF-014-02 — persist day roll + history archive even without a toggle
     * (e.g. Home onResume).
     */
    suspend fun ensureHabitDayRolled() {
        dataStore.edit { prefs ->
            val today = todayEpochDay()
            val decoded = HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON])
            if (decoded == null || decoded.epochDay == today) return@edit
            val rolled = archivePreviousDayIfNeeded(prefs, today)
            prefs[PreferencesKeys.HABIT_DAY_STATE_JSON] = HabitJson.encodeDayState(rolled)
        }
    }

    /** RF-006-01, RF-006-03 — persist Light/Dark theme mode. */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.storageValue
        }
    }

    /** Spec 015 / RF-015-01 — persist clamped Pomodoro config. */
    suspend fun setPomodoroConfig(config: PomodoroConfig) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.POMODORO_CONFIG_JSON] =
                PomodoroJson.encodeConfig(config.clamped())
        }
    }

    /** Spec 015 / RF-015-02 — persist Pomodoro session (idle / running / paused). */
    suspend fun setPomodoroSession(session: PomodoroSession) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.POMODORO_SESSION_JSON] = PomodoroJson.encodeSession(session)
        }
    }

    suspend fun getPomodoroConfig(): PomodoroConfig = pomodoroConfig.first()

    suspend fun getPomodoroSession(): PomodoroSession = pomodoroSession.first()

    /**
     * Spec 021 / RF-021-02, RF-021-03 —
     * Increment completed work count for [dayKey] (`yyyy-MM-dd`) and prune to 30 days.
     */
    suspend fun incrementPomodoroWorkCompleted(dayKey: String = PomodoroDailyLog.dayKey()) {
        dataStore.edit { prefs ->
            val current = PomodoroJson.decodeDaily(prefs[PreferencesKeys.POMODORO_DAILY_JSON])
            val incremented = PomodoroDailyLog.incrementToday(current, dayKey)
            val pruned = PomodoroDailyLog.prune(incremented, dayKey)
            prefs[PreferencesKeys.POMODORO_DAILY_JSON] = PomodoroJson.encodeDaily(pruned)
        }
    }

    /**
     * If stored day ≠ today, append its completions to history (pruned), then return
     * [HabitDayRoll.ensureToday] state. Does not write day state by itself.
     */
    private fun archivePreviousDayIfNeeded(
        prefs: MutablePreferences,
        today: Long,
    ): HabitDayState {
        val decoded = HabitJson.decodeDayState(prefs[PreferencesKeys.HABIT_DAY_STATE_JSON])
        if (decoded != null && decoded.epochDay != today) {
            var history = readHistory(prefs)
            history = HabitHistoryOps.archiveDay(
                history = history,
                epochDay = decoded.epochDay,
                completedIds = decoded.completedIds,
            )
            history = HabitHistoryOps.prune(history, todayEpochDay = today)
            prefs[PreferencesKeys.HABIT_HISTORY_JSON] = HabitJson.encodeHistory(history)
        }
        return HabitDayRoll.ensureToday(decoded, today)
    }

    private fun readHistory(prefs: Preferences): HabitHistory =
        HabitJson.decodeHistory(prefs[PreferencesKeys.HABIT_HISTORY_JSON])

    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}
