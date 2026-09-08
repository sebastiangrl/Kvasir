package app.kvasir.launcher.domain.model

/**
 * Spec 006 / RF-006-01, RF-006-06 — persisted Light/Dark theme (no System mode in v1).
 */
enum class ThemeMode {
    Light,
    Dark,
    ;

    val storageValue: String
        get() = when (this) {
            Light -> "light"
            Dark -> "dark"
        }

    companion object {
        fun fromStorage(raw: String?): ThemeMode =
            when (raw) {
                "dark" -> Dark
                "light" -> Light
                else -> Light // missing/invalid → light (RF-006-06)
            }
    }
}
