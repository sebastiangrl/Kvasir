package app.kvasir.launcher.di

import android.content.Context
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Spec 001 / RF-001-10 + Spec 002 / RF-002-11 —
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
}
