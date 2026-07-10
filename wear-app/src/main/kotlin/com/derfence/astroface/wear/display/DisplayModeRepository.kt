package com.derfence.astroface.wear.display

import android.content.Context

object DisplayModeRepository {
    private const val prefsName = "astroface_display_mode"
    private const val keyMode = "mode"

    fun read(context: Context): DisplayMode {
        val raw = context.preferences().getString(keyMode, null)
        return raw?.let { runCatching { DisplayMode.valueOf(it) }.getOrNull() }
            ?: DisplayMode.FULL_DIAL
    }

    fun write(context: Context, mode: DisplayMode) {
        context.preferences().edit()
            .putString(keyMode, mode.name)
            .apply()
    }

    fun cycleNext(context: Context): DisplayMode {
        val nextMode = read(context).next()
        write(context, nextMode)
        return nextMode
    }

    private fun Context.preferences() =
        applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
}
