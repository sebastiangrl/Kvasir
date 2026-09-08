package app.kvasir.launcher.data.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import app.kvasir.launcher.domain.model.NextCalendarEvent

/**
 * Spec 012 / RF-012-07 — open calendar event or calendar app (best-effort, no crash).
 */
object CalendarEventNavigator {

    private const val TAG = "CalendarEventNavigator"

    fun open(context: Context, event: NextCalendarEvent) {
        val app = context.applicationContext
        val eventId = event.eventId
        if (eventId != null) {
            val viewEvent = Intent(Intent.ACTION_VIEW).apply {
                data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(app, viewEvent)) return
        }
        val openCalendar = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(app, openCalendar)) return
        Log.w(TAG, "no handler for calendar event id=${event.eventId}")
    }

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "startActivity failed: ${intent.action}", t)
            false
        }
    }
}
