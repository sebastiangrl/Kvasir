package app.kvasir.launcher.data.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/**
 * Spec 001 / RF-001-08, RF-001-09 —
 * Detects whether Kvasir is the default Home and builds a reversible picker Intent.
 * Uses Application context only (no Activity / Bitmap caches).
 */
class DefaultHomeRepository(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun isDefaultHome(): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = appContext.packageManager.resolveActivity(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolved?.activityInfo?.packageName == appContext.packageName
    }

    /**
     * RoleManager ROLE_HOME when available (API 29+); otherwise HOME settings.
     * Never forces an irreversible default.
     */
    fun createHomePickerIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = appContext.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }
}
