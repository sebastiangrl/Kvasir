package app.kvasir.launcher.domain.model

/**
 * Spec 014 / RF-014-01 — completed epochDays per habit id (persisted as habit_history_json).
 */
typealias HabitHistory = Map<String, Set<Long>>
