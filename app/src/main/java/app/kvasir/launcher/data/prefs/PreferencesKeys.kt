package app.kvasir.launcher.data.prefs

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * Spec 003 / RF-003-01 + Spec 005 / RF-005-01, RF-005-08 —
 * Preferences schema; additive keys; missing → empty defaults.
 */
object PreferencesKeys {
    /** Stable set of [app.kvasir.launcher.domain.model.InstalledApp.componentKey] values. */
    val FAVORITE_COMPONENT_KEYS = stringSetPreferencesKey("favorite_component_keys")

    /** JSON array of habits `{ id, label }`. */
    val HABITS_JSON = stringPreferencesKey("habits_json")

    /** JSON object `{ epochDay, completedIds }`. */
    val HABIT_DAY_STATE_JSON = stringPreferencesKey("habit_day_state_json")
}
