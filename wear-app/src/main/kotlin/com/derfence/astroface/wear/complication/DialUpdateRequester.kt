package com.derfence.astroface.wear.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.derfence.astroface.wear.status.RequestStatusRepository

object DialUpdateRequester {
    fun requestAll(context: Context) {
        request(context, Dial24hDataSourceService::class.java)
        request(context, Dial12hDataSourceService::class.java)
        request(context, Hour24hHandDataSourceService::class.java)
        RequestStatusRepository.markManualRefresh(context.applicationContext)
    }

    private fun request(
        context: Context,
        serviceClass: Class<out AstroFaceDialDataSourceService>
    ) {
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, serviceClass)
        ).requestUpdateAll()
    }
}
