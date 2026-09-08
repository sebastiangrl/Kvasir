package app.kvasir.launcher.data.prefs

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * Spec 003 + Spec 005 + Spec 006 / RF-006-01, RF-006-06 + Spec 014 / RF-014-01 —
 * Preferences schema; additive keys; missing → empty / light defaults.
 */
object PreferencesKeys {
    /** Stable set of [app.kvasir.launcher.domain.model.InstalledApp.componentKey] values. */
    val FAVORITE_COMPONENT_KEYS = stringSetPreferencesKey("favorite_component_keys")

    /** JSON array of habits `{ id, label }`. */
    val HABITS_JSON = stringPreferencesKey("habits_json")

    /** JSON object `{ epochDay, completedIds }`. */
    val HABIT_DAY_STATE_JSON = stringPreferencesKey("habit_day_state_json")

    /** Spec 014 — JSON object `{ habitId: [epochDay, ...] }`. */
    val HABIT_HISTORY_JSON = stringPreferencesKey("habit_history_json")

    /** Theme mode: `"light"` | `"dark"`; missing → light. */
    val THEME_MODE = stringPreferencesKey("theme_mode")
}
