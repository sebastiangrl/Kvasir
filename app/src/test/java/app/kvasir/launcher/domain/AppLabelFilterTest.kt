package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.HomeListMode
import app.kvasir.launcher.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Spec 008 / RF-008-01…04 + Spec 013 / RF-013-02, RF-013-03, RF-013-06 —
 * JVM tests for label substring filter, overlay policy, and Home modes.
 */
class AppLabelFilterTest {

    private val apps = listOf(
        InstalledApp("com.a", "A", "Alpha"),
        InstalledApp("com.b", "B", "bravo"),
        InstalledApp("com.maps", "M", "Google Maps"),
        InstalledApp("com.accent", "Acc", "Águila"),
        InstalledApp("com.num", "N", "1Password"),
    )

    @Test
    fun matches_blankQueryAlwaysTrue() {
        assertTrue(AppLabelFilter.matches("Alpha", "", Locale.US))
        assertTrue(AppLabelFilter.matches("Alpha", "   ", Locale.US))
    }

    @Test
    fun matches_substringCaseInsensitive() {
        assertTrue(AppLabelFilter.matches("Alpha", "alp", Locale.US))
        assertTrue(AppLabelFilter.matches("Alpha", "PHA", Locale.US))
        assertFalse(AppLabelFilter.matches("Alpha", "bravo", Locale.US))
    }

    @Test
    fun matches_usesLocaleLowercasing() {
        // Turkish dotted/dotless I: "I".lowercase(TR) != "i"
        val tr = Locale.forLanguageTag("tr-TR")
        assertTrue(AppLabelFilter.matches("İstanbul", "ist", tr))
    }

    @Test
    fun filterByQuery_blankReturnsSameList() {
        val result = AppLabelFilter.filterByQuery(apps, "  ", Locale.US)
        assertEquals(apps, result)
    }

    @Test
    fun filterByQuery_substringPreservesOrder() {
        val result = AppLabelFilter.filterByQuery(apps, "a", Locale.US)
        assertEquals(
            listOf("Alpha", "bravo", "Google Maps", "Águila", "1Password"),
            result.map { it.label },
        )
    }

    @Test
    fun filterByQuery_narrowMatch() {
        val result = AppLabelFilter.filterByQuery(apps, "maps", Locale.US)
        assertEquals(listOf("Google Maps"), result.map { it.label })
    }

    @Test
    fun filterByQuery_emptyWhenNoMatch() {
        val result = AppLabelFilter.filterByQuery(apps, "zzzz", Locale.US)
        assertTrue(result.isEmpty())
    }

    @Test
    fun filterOverlay_queryWinsOverLetter() {
        // RF-008-03 — letter 'Z' would be empty; query "map" still finds Maps
        val result = AppLabelFilter.filterOverlay(apps, "map", 'Z', Locale.US)
        assertEquals(listOf("Google Maps"), result.map { it.label })
    }

    @Test
    fun filterOverlay_blankQueryUsesLetterBucket() {
        // RF-008-04
        val result = AppLabelFilter.filterOverlay(apps, "", 'A', Locale.US)
        assertEquals(listOf("Alpha", "Águila"), result.map { it.label })
    }

    @Test
    fun filterHome_favoritesMode_blankQuery_returnsFavorites() {
        val favorites = listOf(apps[0], apps[2])
        val result = AppLabelFilter.filterHome(
            installed = apps,
            favorites = favorites,
            query = "",
            mode = HomeListMode.Favorites,
            locale = Locale.US,
        )
        assertTrue(result.showingFavoritesChrome)
        assertFalse(result.queryActive)
        assertEquals(listOf("Alpha", "Google Maps"), result.apps.map { it.label })
    }

    @Test
    fun filterHome_letterMode_filtersInstalled() {
        val result = AppLabelFilter.filterHome(
            installed = apps,
            favorites = emptyList(),
            query = "",
            mode = HomeListMode.Letter('A'),
            locale = Locale.US,
        )
        assertFalse(result.showingFavoritesChrome)
        assertEquals(listOf("Alpha", "Águila"), result.apps.map { it.label })
    }

    @Test
    fun filterHome_queryWinsOverLetterAndFavorites() {
        val favorites = listOf(apps[0])
        val result = AppLabelFilter.filterHome(
            installed = apps,
            favorites = favorites,
            query = "map",
            mode = HomeListMode.Letter('Z'),
            locale = Locale.US,
        )
        assertFalse(result.showingFavoritesChrome)
        assertTrue(result.queryActive)
        assertEquals(listOf("Google Maps"), result.apps.map { it.label })
    }
}
