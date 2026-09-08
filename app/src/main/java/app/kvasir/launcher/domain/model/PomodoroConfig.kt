package app.kvasir.launcher.domain.model

/**
 * Spec 015 / RF-015-01 — Pomodoro durations and cycle length (minutes).
 */
data class PomodoroConfig(
    val workMinutes: Int = DEFAULT_WORK_MINUTES,
    val breakMinutes: Int = DEFAULT_BREAK_MINUTES,
    val sessionsPerCycle: Int = DEFAULT_SESSIONS,
) {
    fun clamped(): PomodoroConfig =
        PomodoroConfig(
            workMinutes = workMinutes.coerceIn(MIN_WORK, MAX_WORK),
            breakMinutes = breakMinutes.coerceIn(MIN_BREAK, MAX_BREAK),
            sessionsPerCycle = sessionsPerCycle.coerceIn(MIN_SESSIONS, MAX_SESSIONS),
        )

    companion object {
        const val DEFAULT_WORK_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val DEFAULT_SESSIONS = 4

        const val MIN_WORK = 1
        const val MAX_WORK = 90
        const val MIN_BREAK = 1
        const val MAX_BREAK = 30
        const val MIN_SESSIONS = 1
        const val MAX_SESSIONS = 12

        val Default: PomodoroConfig = PomodoroConfig().clamped()
    }
}
