package com.derfence.astroface.wear.store

const val WATCH_FACE_PACKAGE = "com.derfence.astroface.face"

fun watchFaceMarketUri(packageName: String = WATCH_FACE_PACKAGE): String =
    "market://details?id=$packageName"

fun watchFaceWebUri(packageName: String = WATCH_FACE_PACKAGE): String =
    "https://play.google.com/store/apps/details?id=$packageName"

fun interface InstalledPackageLookup {
    fun isInstalled(packageName: String): Boolean
}

class WatchFaceAvailability(
    private val packageLookup: InstalledPackageLookup
) {
    fun isWatchFaceInstalled(): Boolean =
        packageLookup.isInstalled(WATCH_FACE_PACKAGE)
}
