package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.AppShortcut

/**
 * Spec 018 / RF-018-01, RF-018-03 —
 * Pure prepare of shortcuts: non-blank id/label, max [MAX_SHORTCUTS].
 */
object AppShortcutOps {

    const val MAX_SHORTCUTS = 5

    /** JVM-friendly input before Android [android.content.pm.ShortcutInfo]. */
    data class Draft(
        val id: String,
        val packageName: String,
        val shortLabel: String?,
        val longLabel: String? = null,
    )

    fun resolveLabel(shortLabel: String?, longLabel: String?): String {
        val short = shortLabel?.trim().orEmpty()
        if (short.isNotEmpty()) return short
        return longLabel?.trim().orEmpty()
    }

    fun prepare(drafts: List<Draft>, max: Int = MAX_SHORTCUTS): List<AppShortcut> {
        if (max <= 0) return emptyList()
        return drafts
            .asSequence()
            .mapNotNull { draft ->
                val id = draft.id.trim()
                val packageName = draft.packageName.trim()
                val label = resolveLabel(draft.shortLabel, draft.longLabel)
                if (id.isEmpty() || packageName.isEmpty() || label.isEmpty()) {
                    null
                } else {
                    AppShortcut(id = id, packageName = packageName, label = label)
                }
            }
            .take(max)
            .toList()
    }
}
