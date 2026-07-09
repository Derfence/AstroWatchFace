package com.derfence.astroface.wear.store

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

class PlayStoreNavigator(
    private val context: Context
) {
    fun openWatchFaceStore(): Boolean =
        open(watchFaceMarketUri()) || open(watchFaceWebUri())

    private fun open(uri: String): Boolean =
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
}
