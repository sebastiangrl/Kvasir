package app.kvasir.launcher.di

import android.content.Context
import app.kvasir.launcher.data.apps.AppIconLoader
import app.kvasir.launcher.data.apps.AppShortcutsRepository
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.calendar.CalendarEventsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.notifications.NotificationBadgeRepository
import app.kvasir.launcher.data.pomodoro.PomodoroAlarmScheduler
import app.kvasir.launcher.data.pomodoro.PomodoroController
import app.kvasir.launcher.data.pomodoro.PomodoroNotifier
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.data.settings.PermissionsStatusHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Spec 001 / RF-001-10 + Spec 002 / RF-002-11 + Spec 003 / RF-003-01 + Spec 010 / RF-010-05 +
 * Spec 012 / RF-012-05 + Spec 015 / RF-015-05 + Spec 016 / RF-016-04 + Spec 017 / RF-017-01 +
 * Spec 018 / RF-018-01 —
 * Manual DI; Application context only (no Activity leaks).
 */
class AppContainer(
    applicationContext: Context,
) {
    val appContext: Context = applicationContext.applicationContext

    /** Process-lifetime scope for launcher background work (not cancelled by Activity). */
    val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val defaultHomeRepository: DefaultHomeRepository =
        DefaultHomeRepository(appContext)

    val launcherAppsRepository: LauncherAppsRepository =
        LauncherAppsRepository(
            context = appContext,
            scope = applicationScope,
        )

    /** Spec 018 — app shortcuts (query / start); no Compose. */
    val appShortcutsRepository: AppShortcutsRepository =
        AppShortcutsRepository(appContext)

    val preferencesRepository: PreferencesRepository =
        PreferencesRepository(appContext)

    /** Spec 010 — activity icons for monochrome UI (not embedded in InstalledApp). */
    val appIconLoader: AppIconLoader = AppIconLoader(appContext)

    /** Spec 012 — next calendar event (READ_CALENDAR; no Compose ContentResolver). */
    val calendarEventsRepository: CalendarEventsRepository =
        CalendarEventsRepository(appContext)

    /** Spec 015 — Pomodoro alarms / notifications / prefs orchestration. */
    val pomodoroAlarmScheduler: PomodoroAlarmScheduler =
        PomodoroAlarmScheduler(appContext)

    val pomodoroNotifier: PomodoroNotifier =
        PomodoroNotifier(appContext)

    val pomodoroController: PomodoroController =
        PomodoroController(
            preferencesRepository = preferencesRepository,
            scheduler = pomodoroAlarmScheduler,
            notifier = pomodoroNotifier,
        )

    /** Spec 017 — packages with badge-worthy active notifications (memory only). */
    val notificationBadgeRepository: NotificationBadgeRepository =
        NotificationBadgeRepository(appContext)

    /** Spec 016 + Spec 017 — permission / default-Home / listener status for Settings hub. */
    val permissionsStatusHelper: PermissionsStatusHelper =
        PermissionsStatusHelper(
            context = appContext,
            defaultHomeRepository = defaultHomeRepository,
            notificationBadgeRepository = notificationBadgeRepository,
        )
}
