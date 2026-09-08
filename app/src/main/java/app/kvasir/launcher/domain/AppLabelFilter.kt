package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.InstalledApp
import java.util.Locale

/**
 * Spec 008 / RF-008-01…04 —
 * Pure label substring filter (locale case-insensitive). No package matching.
 */
object AppLabelFilter {

    /** True if [query] is blank or [label] contains it (case-insensitive, [locale]). */
    fun matches(
        label: String,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        return label.lowercase(locale).contains(trimmed.lowercase(locale))
    }

    /**
     * Apps whose [InstalledApp.label] matches [query]; blank query returns [apps] unchanged.
     * Preserves input order.
     */
    fun filterByQuery(
        apps: List<InstalledApp>,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): List<InstalledApp> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return apps
        return apps.filter { matches(it.label, trimmed, locale) }
    }

    /**
     * Overlay list policy (RF-008-03, RF-008-04): non-blank query ignores [letter];
     * blank query uses [LetterBucket.filterByLetter].
     */
    fun filterOverlay(
        apps: List<InstalledApp>,
        query: String,
        letter: Char,
        locale: Locale = Locale.getDefault(),
    ): List<InstalledApp> {
        return if (query.trim().isNotEmpty()) {
            filterByQuery(apps, query, locale)
        } else {
            LetterBucket.filterByLetter(apps, letter, locale)
        }
    }
}
