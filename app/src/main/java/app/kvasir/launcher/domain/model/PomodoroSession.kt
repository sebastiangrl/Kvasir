package app.kvasir.launcher.domain.model

/**
 * Spec 015 / RF-015-02 — Active Pomodoro session (idle / running / paused).
 */
enum class PomodoroStatus {
    Idle,
    Running,
    Paused,
}

enum class PomodoroPhase {
    Work,
    Break,
}

data class PomodoroSession(
    val status: PomodoroStatus = PomodoroStatus.Idle,
    val phase: PomodoroPhase = PomodoroPhase.Work,
    /** 1-based index of the current (or just-finished) work session in the cycle. */
    val sessionIndex: Int = 1,
    val endsAtEpochMillis: Long? = null,
    /** Set while [PomodoroStatus.Paused]; cleared when running/idle. */
    val remainingMillis: Long? = null,
) {
    companion object {
        val Idle: PomodoroSession = PomodoroSession()
    }
}

/** Spec 015 / RF-015-05, RF-015-08 — side-effect hint after a domain transition. */
enum class PomodoroNotifyEvent {
    WorkFinished,
    BreakFinished,
    CycleComplete,
}
