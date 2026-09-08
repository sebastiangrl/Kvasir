package app.kvasir.launcher.di

import android.content.Context
import app.kvasir.launcher.data.home.DefaultHomeRepository

/**
 * Spec 001 / RF-001-10 — manual DI; Application context only (no Activity leaks).
 */
class AppContainer(
    applicationContext: Context,
) {
    val appContext: Context = applicationContext.applicationContext

    val defaultHomeRepository: DefaultHomeRepository =
        DefaultHomeRepository(appContext)
}
