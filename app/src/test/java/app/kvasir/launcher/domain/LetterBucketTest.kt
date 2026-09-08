package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Spec 004 / RF-004-04, RF-004-05 — JVM tests for letter buckets and filter.
 */
class LetterBucketTest {

    private val apps = listOf(
        InstalledApp("com.a", "A", "Alpha"),
        InstalledApp("com.b", "B", "bravo"),
        InstalledApp("com.accent", "Acc", "Águila"),
        InstalledApp("com.num", "N", "1Password"),
        InstalledApp("com.sym", "S", "!!! Tools"),
        InstalledApp("com.z", "Z", "zebra"),
    )

    @Test
    fun keys_areAtoZThenHash() {
        assertEquals(27, LetterBucket.KEYS.size)
        assertEquals('A', LetterBucket.KEYS.first())
        assertEquals('Z', LetterBucket.KEYS[25])
        assertEquals(LetterBucket.OTHER, LetterBucket.KEYS.last())
    }

    @Test
    fun bucketForLabel_mapsLettersCaseInsensitive() {
        assertEquals('A', LetterBucket.bucketForLabel("Alpha", Locale.US))
        assertEquals('B', LetterBucket.bucketForLabel("bravo", Locale.US))
        assertEquals('Z', LetterBucket.bucketForLabel("zebra", Locale.US))
    }

    @Test
    fun bucketForLabel_stripsDiacriticsToBaseLetter() {
        assertEquals('A', LetterBucket.bucketForLabel("Águila", Locale.forLanguageTag("es-ES")))
    }

    @Test
    fun bucketForLabel_nonLettersGoToOther() {
        assertEquals(LetterBucket.OTHER, LetterBucket.bucketForLabel("1Password", Locale.US))
        assertEquals(LetterBucket.OTHER, LetterBucket.bucketForLabel("!!! Tools", Locale.US))
        assertEquals(LetterBucket.OTHER, LetterBucket.bucketForLabel("   ", Locale.US))
        assertEquals(LetterBucket.OTHER, LetterBucket.bucketForLabel("", Locale.US))
    }

    @Test
    fun filterByLetter_returnsMatchingAppsInOrder() {
        val result = LetterBucket.filterByLetter(apps, 'A', Locale.US)
        assertEquals(listOf("Alpha", "Águila"), result.map { it.label })
    }

    @Test
    fun filterByLetter_otherBucket() {
        val result = LetterBucket.filterByLetter(apps, LetterBucket.OTHER, Locale.US)
        assertEquals(listOf("1Password", "!!! Tools"), result.map { it.label })
    }

    @Test
    fun filterByLetter_emptyWhenNoAppsForLetter() {
        // RF-004-05
        val result = LetterBucket.filterByLetter(apps, 'Q', Locale.US)
        assertTrue(result.isEmpty())
    }
}
