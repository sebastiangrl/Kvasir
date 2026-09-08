package app.kvasir.launcher.data.prefs

import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * Spec 003 / RF-003-01, RF-003-09 —
 * First Preferences schema; no migration. Default for missing keys = empty set.
 */
object PreferencesKeys {
    /** Stable set of [app.kvasir.launcher.domain.model.InstalledApp.componentKey] values. */
    val FAVORITE_COMPONENT_KEYS = stringSetPreferencesKey("favorite_component_keys")
}
