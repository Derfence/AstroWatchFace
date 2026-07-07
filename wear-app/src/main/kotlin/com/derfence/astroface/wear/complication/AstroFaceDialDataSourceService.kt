package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository

abstract class AstroFaceDialDataSourceService : ComplicationDataSourceService() {
    protected abstract val renderer: DialRenderer
    protected abstract val statusKey: RequestStatusRepository.DialKey

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        RequestStatusRepository.markRequest(applicationContext, statusKey, request)

        val data = if (request.complicationType == ComplicationType.PHOTO_IMAGE) {
            DialComplicationDataFactory.create(renderer)
        } else {
            NoDataComplicationData()
        }
        listener.onComplicationData(data)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.PHOTO_IMAGE) {
            DialComplicationDataFactory.create(renderer)
        } else {
            NoDataComplicationData()
        }
}
