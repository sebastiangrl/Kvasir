package app.kvasir.launcher.domain

import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus
import org.json.JSONObject

/**
 * Spec 015 / RF-015-01, RF-015-02 + Spec 021 / RF-021-02 —
 * org.json codec for Pomodoro config, session, and daily counts; corrupt → defaults / idle / empty.
 */
object PomodoroJson {

    fun encodeConfig(config: PomodoroConfig): String {
        val c = config.clamped()
        return JSONObject()
            .put("workMinutes", c.workMinutes)
            .put("breakMinutes", c.breakMinutes)
            .put("sessionsPerCycle", c.sessionsPerCycle)
            .toString()
    }

    fun decodeConfig(raw: String?): PomodoroConfig {
        if (raw.isNullOrBlank()) return PomodoroConfig.Default
        return try {
            val obj = JSONObject(raw)
            PomodoroConfig(
                workMinutes = obj.optInt("workMinutes", PomodoroConfig.DEFAULT_WORK_MINUTES),
                breakMinutes = obj.optInt("breakMinutes", PomodoroConfig.DEFAULT_BREAK_MINUTES),
                sessionsPerCycle = obj.optInt("sessionsPerCycle", PomodoroConfig.DEFAULT_SESSIONS),
            ).clamped()
        } catch (_: Exception) {
            PomodoroConfig.Default
        }
    }

    fun encodeSession(session: PomodoroSession): String {
        val obj = JSONObject()
            .put("status", session.status.name)
            .put("phase", session.phase.name)
            .put("sessionIndex", session.sessionIndex)
        session.endsAtEpochMillis?.let { obj.put("endsAtEpochMillis", it) }
        session.remainingMillis?.let { obj.put("remainingMillis", it) }
        return obj.toString()
    }

    fun decodeSession(raw: String?): PomodoroSession {
        if (raw.isNullOrBlank()) return PomodoroSession.Idle
        return try {
            val obj = JSONObject(raw)
            val status = PomodoroStatus.entries.find {
                it.name == obj.optString("status", "")
            } ?: return PomodoroSession.Idle
            val phase = PomodoroPhase.entries.find {
                it.name == obj.optString("phase", "")
            } ?: PomodoroPhase.Work
            val index = obj.optInt("sessionIndex", 1).coerceAtLeast(1)
            val endsAt = if (obj.has("endsAtEpochMillis")) {
                obj.getLong("endsAtEpochMillis")
            } else {
                null
            }
            val remaining = if (obj.has("remainingMillis")) {
                obj.getLong("remainingMillis")
            } else {
                null
            }
            PomodoroSession(
                status = status,
                phase = phase,
                sessionIndex = index,
                endsAtEpochMillis = endsAt,
                remainingMillis = remaining,
            )
        } catch (_: Exception) {
            PomodoroSession.Idle
        }
    }

    /** Spec 021 / RF-021-02 — dayKey → completed work count; corrupt → empty. */
    fun encodeDaily(map: Map<String, Int>): String {
        val obj = JSONObject()
        map.forEach { (day, count) ->
            if (count > 0) obj.put(day, count)
        }
        return obj.toString()
    }

    fun decodeDaily(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    val count = obj.optInt(key, 0)
                    if (count > 0) put(key, count)
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
