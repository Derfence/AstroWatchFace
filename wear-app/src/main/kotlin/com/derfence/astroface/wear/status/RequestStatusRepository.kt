package com.derfence.astroface.wear.status

import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RequestStatusRepository {
    private const val prefsName = "astroface_requests"
    private const val key24h = "last_24h"
    private const val key12h = "last_12h"
    private const val keyManualRefresh = "last_manual_refresh"

    enum class DialKey {
        DIAL_24H,
        DIAL_12H
    }

    data class Status(
        val last24h: String?,
        val last12h: String?,
        val lastManualRefresh: String?
    )

    fun markRequest(context: Context, dialKey: DialKey, request: ComplicationRequest) {
        val value = "${timestamp()} - instance ${request.complicationInstanceId}, " +
            "type ${request.complicationType}"
        context.preferences().edit()
            .putString(if (dialKey == DialKey.DIAL_24H) key24h else key12h, value)
            .apply()
    }

    fun markManualRefresh(context: Context) {
        context.preferences().edit()
            .putString(keyManualRefresh, timestamp())
            .apply()
    }

    fun read(context: Context): Status {
        val prefs = context.preferences()
        return Status(
            last24h = prefs.getString(key24h, null),
            last12h = prefs.getString(key12h, null),
            lastManualRefresh = prefs.getString(keyManualRefresh, null)
        )
    }

    private fun Context.preferences() =
        getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).format(Date())
}
