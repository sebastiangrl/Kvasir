package app.kvasir.launcher.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_FILE = "kvasir_preferences"

private val Context.kvasirDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_FILE,
)

/**
 * Spec 003 / RF-003-01, RF-003-08, RF-003-09 —
 * Preferences DataStore for favorites; Application context only.
 */
class PreferencesRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.kvasirDataStore

    /** Emits the persisted favorite component keys; missing key → empty set (RF-003-09). */
    val favoriteKeys: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.FAVORITE_COMPONENT_KEYS] ?: emptySet()
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
}
