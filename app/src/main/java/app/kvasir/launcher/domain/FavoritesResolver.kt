package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.InstalledApp
import java.text.Collator
import java.util.Locale

/**
 * Spec 003 / RF-003-02, RF-003-09 —
 * Pure: favorites ∩ installed, sorted by label (Collator PRIMARY, same as 002).
 */
object FavoritesResolver {

    fun resolve(
        favoriteKeys: Set<String>,
        installed: List<InstalledApp>,
        locale: Locale = Locale.getDefault(),
    ): List<InstalledApp> {
        if (favoriteKeys.isEmpty()) return emptyList()
        val byKey = installed.associateBy { it.componentKey }
        val collator = Collator.getInstance(locale).apply {
            strength = Collator.PRIMARY
        }
        return favoriteKeys
            .mapNotNull { byKey[it] }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
    }
}
