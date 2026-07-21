package com.derfence.astroface.wear.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.derfence.astroface.wear.status.RequestStatusRepository

object DialUpdateRequester {
    fun requestAll(context: Context) {
        request(context, DialUpdatePlans.ALL)
    }

    fun requestModeOverlay(context: Context) {
        request(context, DialUpdatePlans.MODE_ONLY)
    }

    private fun request(
        context: Context,
        plan: DialUpdatePlan
    ) {
        plan.serviceClasses.forEach { requestService(context, it) }
        if (plan.marksManualRefresh) {
            RequestStatusRepository.markManualRefresh(context.applicationContext)
        }
    }

    private fun requestService(
        context: Context,
        serviceClass: Class<out ComplicationDataSourceService>
    ) {
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, serviceClass)
        ).requestUpdateAll()
    }
}

internal data class DialUpdatePlan(
    val serviceClasses: List<Class<out ComplicationDataSourceService>>,
    val marksManualRefresh: Boolean
)

internal object DialUpdatePlans {
    val ALL = DialUpdatePlan(
        serviceClasses = listOf(
            ConstellationBackgroundDataSourceService::class.java,
            Dial24hDataSourceService::class.java,
            CelestialHorizonDataSourceService::class.java,
            CelestialInnerMotionDataSourceService::class.java,
            CelestialMiddleMotionDataSourceService::class.java,
            CelestialOuterMotionDataSourceService::class.java,
            StatusOverlayDataSourceService::class.java,
            ModeOverlayDataSourceService::class.java
        ),
        marksManualRefresh = true
    )

    val MODE_ONLY = DialUpdatePlan(
        serviceClasses = listOf(ModeOverlayDataSourceService::class.java),
        marksManualRefresh = false
    )
}
