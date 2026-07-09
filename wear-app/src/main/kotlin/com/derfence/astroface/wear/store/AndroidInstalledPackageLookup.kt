package com.derfence.astroface.wear.store

import android.content.pm.PackageManager

class AndroidInstalledPackageLookup(
    private val packageManager: PackageManager
) : InstalledPackageLookup {
    override fun isInstalled(packageName: String): Boolean =
        try {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}
