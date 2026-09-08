package app.kvasir.launcher.data.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import app.kvasir.launcher.domain.model.NextCalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Spec 012 / RF-012-03…05, RF-012-08 —
 * Reads next calendar instance via [CalendarContract.Instances] (Application context only).
 * No WRITE_CALENDAR. Best-effort: missing permission / errors / empty → null.
 */
class CalendarEventsRepository(
    context: Context,
) {
    private val appContext = context.applicationContext

    suspend fun nextEvent(
        nowMillis: Long = System.currentTimeMillis(),
        horizonDays: Int = DEFAULT_HORIZON_DAYS,
    ): NextCalendarEvent? = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) return@withContext null
        try {
            queryNextInstance(nowMillis, horizonDays)
        } catch (t: Throwable) {
            Log.w(TAG, "nextEvent query failed", t)
            null
        }
    }

    fun hasReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun queryNextInstance(nowMillis: Long, horizonDays: Int): NextCalendarEvent? {
        val endMillis = nowMillis + TimeUnit.DAYS.toMillis(horizonDays.toLong())
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().let { builder ->
            ContentUris.appendId(builder, nowMillis)
            ContentUris.appendId(builder, endMillis)
            builder.build()
        }

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        )

        // Visible instances that have not ended yet; earliest begin first.
        val selection = "${CalendarContract.Instances.END} > ?"
        val selectionArgs = arrayOf(nowMillis.toString())
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        appContext.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val allDayIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)

            while (cursor.moveToNext()) {
                val title = cursor.getString(titleIdx)?.trim().orEmpty()
                if (title.isEmpty()) continue
                val begin = cursor.getLong(beginIdx)
                val allDay = cursor.getInt(allDayIdx) == 1
                val eventId = if (cursor.isNull(idIdx)) null else cursor.getLong(idIdx)
                return NextCalendarEvent(
                    title = title,
                    beginEpochMillis = begin,
                    allDay = allDay,
                    eventId = eventId,
                )
            }
        }
        return null
    }

    companion object {
        private const val TAG = "CalendarEventsRepo"
        const val DEFAULT_HORIZON_DAYS = 14
    }
}
