package com.derfence.astroface.wear.status

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.roundToInt

class AndroidBatteryStatusSource(
    private val context: Context
) : BatteryStatusSource {
    override fun currentBatteryStatus(): BatteryStatus =
        BatteryStatus.fromPercent(capacityFromBatteryManager() ?: capacityFromStickyBroadcast())

    private fun capacityFromBatteryManager(): Int? {
        val batteryManager = context.getSystemService(BatteryManager::class.java) ?: return null
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .takeIf { it in MIN_PERCENT..MAX_PERCENT }
    }

    private fun capacityFromStickyBroadcast(): Int? {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return null
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, UNKNOWN_VALUE)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, UNKNOWN_VALUE)

        if (level < 0 || scale <= 0) {
            return null
        }

        return (level * 100.0 / scale).roundToInt().coerceIn(MIN_PERCENT, MAX_PERCENT)
    }

    private companion object {
        private const val MIN_PERCENT = 0
        private const val MAX_PERCENT = 100
        private const val UNKNOWN_VALUE = -1
    }
}
