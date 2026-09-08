package app.kvasir.launcher.domain

import app.kvasir.launcher.data.prefs.PreferencesKeys
import app.kvasir.launcher.domain.model.InstalledApp
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Spec 003 / RF-003-01, RF-003-02, RF-003-09 — JVM tests for schema key + resolve.
 */
class FavoritesResolverTest {

    private val installed = listOf(
        InstalledApp("com.example.z", "ZActivity", "zebra"),
        InstalledApp("com.example.a", "AActivity", "Alpha"),
        InstalledApp("com.example.m", "MActivity", "mango"),
        InstalledApp("com.example.gone", "Gone", "Ghost"),
    )

    @Test
    fun favoriteKey_nameMatchesSpec() {
        // RF-003-01 — documented Preferences key
        assertEquals(
            stringSetPreferencesKey("favorite_component_keys"),
            PreferencesKeys.FAVORITE_COMPONENT_KEYS,
        )
    }

    @Test
    fun resolve_emptyFavorites_returnsEmpty() {
        // RF-003-09 — default empty set
        val result = FavoritesResolver.resolve(
            favoriteKeys = emptySet(),
            installed = installed,
            locale = Locale.US,
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun resolve_intersectsAndSortsByLabel() {
        // RF-003-02
        val keys = setOf(
            "com.example.z/ZActivity",
            "com.example.a/AActivity",
            "com.example.m/MActivity",
        )
        val result = FavoritesResolver.resolve(
            favoriteKeys = keys,
            installed = installed,
            locale = Locale.US,
        )
        assertEquals(listOf("Alpha", "mango", "zebra"), result.map { it.label })
    }

    @Test
    fun resolve_dropsOrphanKeys() {
        // RF-003-02 — uninstalled favorites not shown
        val keys = setOf(
            "com.example.a/AActivity",
            "com.missing/Missing",
        )
        val result = FavoritesResolver.resolve(
            favoriteKeys = keys,
            installed = installed.filter { it.packageName != "com.example.gone" },
            locale = Locale.US,
        )
        assertEquals(listOf("Alpha"), result.map { it.label })
    }
}
