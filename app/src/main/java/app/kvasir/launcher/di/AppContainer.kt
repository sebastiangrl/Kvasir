package app.kvasir.launcher.di

import android.content.Context
import app.kvasir.launcher.data.apps.AppIconLoader
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Spec 001 / RF-001-10 + Spec 002 / RF-002-11 + Spec 003 / RF-003-01 + Spec 010 / RF-010-05 —
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

    val preferencesRepository: PreferencesRepository =
        PreferencesRepository(appContext)

    /** Spec 010 — activity icons for monochrome UI (not embedded in InstalledApp). */
    val appIconLoader: AppIconLoader = AppIconLoader(appContext)
}
