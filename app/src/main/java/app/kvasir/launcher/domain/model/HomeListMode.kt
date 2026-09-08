package app.kvasir.launcher.domain.model

/**
 * Spec 013 / RF-013-01…03 — Home scrubber mode (★ favorites vs letter bucket).
 * Ephemeral; not persisted.
 */
sealed class HomeListMode {
    data object Favorites : HomeListMode()

    data class Letter(val letter: Char) : HomeListMode()
}
