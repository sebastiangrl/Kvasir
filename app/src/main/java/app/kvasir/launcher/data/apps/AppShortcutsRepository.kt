package app.kvasir.launcher.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.util.Log
import app.kvasir.launcher.domain.AppShortcutOps
import app.kvasir.launcher.domain.model.AppShortcut

/**
 * Spec 018 / RF-018-01, RF-018-04, RF-018-05 —
 * Query / start app shortcuts via [LauncherApps]; Application context only.
 */
class AppShortcutsRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val launcherApps =
        appContext.getSystemService(LauncherApps::class.java)
            ?: error("LauncherApps service unavailable")
    private val userHandle = Process.myUserHandle()

    fun shortcutsFor(packageName: String): List<AppShortcut> {
        if (packageName.isBlank()) return emptyList()
        return try {
            val query = LauncherApps.ShortcutQuery()
                .setPackage(packageName)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            val raw = launcherApps.getShortcuts(query, userHandle).orEmpty()
            AppShortcutOps.prepare(raw.map { it.toDraft() })
        } catch (t: Throwable) {
            Log.w(TAG, "getShortcuts failed for $packageName", t)
            emptyList()
        }
    }

    fun startShortcut(shortcut: AppShortcut) {
        try {
            launcherApps.startShortcut(
                shortcut.packageName,
                shortcut.id,
                null,
                null,
                userHandle,
            )
        } catch (t: Throwable) {
            Log.w(TAG, "startShortcut failed ${shortcut.packageName}/${shortcut.id}", t)
        }
    }

    private fun ShortcutInfo.toDraft(): AppShortcutOps.Draft =
        AppShortcutOps.Draft(
            id = id.orEmpty(),
            packageName = `package`.orEmpty(),
            shortLabel = shortLabel?.toString(),
            longLabel = longLabel?.toString(),
        )

    private companion object {
        const val TAG = "AppShortcutsRepo"
    }
}
