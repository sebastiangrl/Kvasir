package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.InstalledApp
import java.text.Normalizer
import java.util.Locale

/**
 * Spec 004 / RF-004-04, RF-004-05 —
 * Pure letter bucket for scrubber: A–Z or '#' for non-letter initials (locale-aware).
 */
object LetterBucket {

    const val OTHER: Char = '#'

    /** Scrubber keys in order: A…Z then #. */
    val KEYS: List<Char> = (('A'..'Z') + OTHER).toList()

    /**
     * Initial bucket for [label]: uppercase Latin A–Z after NFD diacritic strip,
     * else [OTHER]. Empty/blank → [OTHER].
     */
    fun bucketForLabel(label: String, locale: Locale = Locale.getDefault()): Char {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return OTHER
        val upper = trimmed.substring(0, 1).uppercase(locale)
        val base = Normalizer.normalize(upper, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
        val c = base.firstOrNull() ?: return OTHER
        return if (c in 'A'..'Z') c else OTHER
    }

    /** Apps whose [InstalledApp.label] maps to [letter]; preserves input order. */
    fun filterByLetter(
        apps: List<InstalledApp>,
        letter: Char,
        locale: Locale = Locale.getDefault(),
    ): List<InstalledApp> {
        val key = letter.uppercaseChar().let { c ->
            if (c in 'A'..'Z' || c == OTHER) c else OTHER
        }
        return apps.filter { bucketForLabel(it.label, locale) == key }
    }

    private val DIACRITICS = "\\p{M}+".toRegex()
}
