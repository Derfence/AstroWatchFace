package com.derfence.astroface.wear.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.derfence.astroface.wear.status.RequestStatusRepository

object DialUpdateRequester {
    fun requestAll(context: Context) {
        request(context, ConstellationBackgroundDataSourceService::class.java)
        request(context, Dial24hDataSourceService::class.java)
        request(context, CelestialHorizonDataSourceService::class.java)
        request(context, CelestialInnerMotionDataSourceService::class.java)
        request(context, CelestialMiddleMotionDataSourceService::class.java)
        request(context, CelestialOuterMotionDataSourceService::class.java)
        request(context, StatusOverlayDataSourceService::class.java)
        request(context, ModeOverlayDataSourceService::class.java)
        RequestStatusRepository.markManualRefresh(context.applicationContext)
    }

    private fun request(
        context: Context,
        serviceClass: Class<out ComplicationDataSourceService>
    ) {
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, serviceClass)
        ).requestUpdateAll()
    }
}
