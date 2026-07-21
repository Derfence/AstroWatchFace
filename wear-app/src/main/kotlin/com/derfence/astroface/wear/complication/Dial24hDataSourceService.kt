package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Instant

class Dial24hDataSourceService : ComplicationDataSourceService() {
    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        RequestStatusRepository.markRequest(
            applicationContext,
            RequestStatusRepository.DialKey.DIAL_24H,
            request
        )
        if (request.complicationType != ComplicationType.RANGED_VALUE) {
            listener.onComplicationData(NoDataComplicationData())
            return
        }
        listener.onComplicationDataTimeline(
            Dial24hComplicationDataFactory.createTimeline(Instant.now())
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        if (type == ComplicationType.RANGED_VALUE) {
            Dial24hComplicationDataFactory.createCurrent(Instant.now())
        } else {
            NoDataComplicationData()
        }
}
