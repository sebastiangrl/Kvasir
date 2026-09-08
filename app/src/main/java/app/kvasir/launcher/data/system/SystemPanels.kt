package app.kvasir.launcher.data.system

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

/**
 * Spec 009 / RF-009-01, RF-009-07 —
 * Best-effort expand of Quick Settings, then notifications. Never crashes Home.
 */
object SystemPanels {

    private const val TAG = "SystemPanels"
    private const val STATUS_BAR_SERVICE = "statusbar"

    /**
     * Prefer Quick Settings (`expandSettingsPanel`); fall back to notifications.
     * No-op if both fail (OEM / API / permission).
     */
    @SuppressLint("WrongConstant")
    fun expandPreferredPanel(context: Context) {
        try {
            val service = context.applicationContext.getSystemService(STATUS_BAR_SERVICE)
                ?: return
            if (invokeExpand(service, "expandSettingsPanel")) return
            invokeExpand(service, "expandNotificationsPanel")
        } catch (t: Throwable) {
            Log.w(TAG, "expandPreferredPanel failed", t)
        }
    }

    private fun invokeExpand(statusBarService: Any, methodName: String): Boolean {
        return try {
            val method = statusBarService.javaClass.getMethod(methodName)
            method.invoke(statusBarService)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "invoke $methodName failed", t)
            false
        }
    }
}
