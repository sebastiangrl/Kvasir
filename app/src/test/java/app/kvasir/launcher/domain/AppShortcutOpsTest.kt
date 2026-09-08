package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 018 / RF-018-01, RF-018-03 — prepare shortcuts (max 5, labels).
 */
class AppShortcutOpsTest {

    @Test
    fun resolveLabel_prefersShortThenLong() {
        assertEquals("Short", AppShortcutOps.resolveLabel(" Short ", "Long"))
        assertEquals("Long", AppShortcutOps.resolveLabel("  ", " Long "))
        assertEquals("", AppShortcutOps.resolveLabel(null, null))
    }

    @Test
    fun prepare_skipsBlankIdPackageOrLabel() {
        val drafts = listOf(
            AppShortcutOps.Draft(id = "", packageName = "a.b", shortLabel = "X"),
            AppShortcutOps.Draft(id = "1", packageName = "", shortLabel = "X"),
            AppShortcutOps.Draft(id = "2", packageName = "a.b", shortLabel = "  "),
            AppShortcutOps.Draft(id = "3", packageName = "a.b", shortLabel = "Ok"),
        )
        val result = AppShortcutOps.prepare(drafts)
        assertEquals(1, result.size)
        assertEquals("3", result[0].id)
        assertEquals("Ok", result[0].label)
    }

    @Test
    fun prepare_capsAtMaxShortcuts() {
        val drafts = (1..8).map { i ->
            AppShortcutOps.Draft(
                id = "id$i",
                packageName = "com.example",
                shortLabel = "Label $i",
            )
        }
        val result = AppShortcutOps.prepare(drafts)
        assertEquals(AppShortcutOps.MAX_SHORTCUTS, result.size)
        assertEquals("id1", result.first().id)
        assertEquals("id5", result.last().id)
    }

    @Test
    fun prepare_emptyInput() {
        assertTrue(AppShortcutOps.prepare(emptyList()).isEmpty())
    }
}
