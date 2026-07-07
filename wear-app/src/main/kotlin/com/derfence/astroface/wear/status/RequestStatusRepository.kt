package com.derfence.astroface.wear.status

import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RequestStatusRepository {
    private const val prefsName = "astroface_requests"
    private const val key24h = "last_24h"
    private const val key24hHand = "last_24h_hand"
    private const val keyManualRefresh = "last_manual_refresh"

    enum class DialKey {
        DIAL_24H,
        HOUR_24H_HAND
    }

    data class Status(
        val last24h: String?,
        val last24hHand: String?,
        val lastManualRefresh: String?
    )

    fun markRequest(context: Context, dialKey: DialKey, request: ComplicationRequest) {
        val value = "${timestamp()} - instance ${request.complicationInstanceId}, " +
            "type ${request.complicationType}"
        context.preferences().edit()
            .putString(keyFor(dialKey), value)
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
            last24hHand = prefs.getString(key24hHand, null),
            lastManualRefresh = prefs.getString(keyManualRefresh, null)
        )
    }

    private fun keyFor(dialKey: DialKey): String =
        when (dialKey) {
            DialKey.DIAL_24H -> key24h
            DialKey.HOUR_24H_HAND -> key24hHand
        }

    private fun Context.preferences() =
        getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).format(Date())
}
