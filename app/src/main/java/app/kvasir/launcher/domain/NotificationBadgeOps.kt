package app.kvasir.launcher.domain

/**
 * Spec 017 / RF-017-01 —
 * Pure: which packages deserve a Home badge from an active-notification snapshot.
 * Excludes ongoing (media/nav) and group summaries.
 */
object NotificationBadgeOps {

    data class ActiveNotif(
        val packageName: String,
        val isOngoing: Boolean,
        val isGroupSummary: Boolean,
    )

    fun packagesWithBadge(active: List<ActiveNotif>): Set<String> =
        active
            .asSequence()
            .filter { it.packageName.isNotEmpty() }
            .filter { !it.isOngoing && !it.isGroupSummary }
            .map { it.packageName }
            .toSet()
}
