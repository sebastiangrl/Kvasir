package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 017 / RF-017-01 — package set from active notifications (no Android SBN).
 */
class NotificationBadgeOpsTest {

    @Test
    fun packagesWithBadge_includesNormalPosts() {
        val active = listOf(
            NotificationBadgeOps.ActiveNotif("com.chat", isOngoing = false, isGroupSummary = false),
            NotificationBadgeOps.ActiveNotif("com.mail", isOngoing = false, isGroupSummary = false),
        )
        assertEquals(setOf("com.chat", "com.mail"), NotificationBadgeOps.packagesWithBadge(active))
    }

    @Test
    fun packagesWithBadge_excludesOngoingAndGroupSummary() {
        val active = listOf(
            NotificationBadgeOps.ActiveNotif("com.music", isOngoing = true, isGroupSummary = false),
            NotificationBadgeOps.ActiveNotif("com.chat", isOngoing = false, isGroupSummary = true),
            NotificationBadgeOps.ActiveNotif("com.chat", isOngoing = false, isGroupSummary = false),
        )
        assertEquals(setOf("com.chat"), NotificationBadgeOps.packagesWithBadge(active))
    }

    @Test
    fun packagesWithBadge_skipsBlankPackage() {
        val active = listOf(
            NotificationBadgeOps.ActiveNotif("", isOngoing = false, isGroupSummary = false),
        )
        assertTrue(NotificationBadgeOps.packagesWithBadge(active).isEmpty())
    }

    @Test
    fun packagesWithBadge_dedupesSamePackage() {
        val active = listOf(
            NotificationBadgeOps.ActiveNotif("com.chat", isOngoing = false, isGroupSummary = false),
            NotificationBadgeOps.ActiveNotif("com.chat", isOngoing = false, isGroupSummary = false),
        )
        assertEquals(setOf("com.chat"), NotificationBadgeOps.packagesWithBadge(active))
    }
}
