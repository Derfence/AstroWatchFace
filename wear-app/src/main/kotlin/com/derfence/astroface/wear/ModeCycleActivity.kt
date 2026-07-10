package com.derfence.astroface.wear

import android.app.Activity
import android.os.Bundle
import com.derfence.astroface.wear.complication.DialUpdateRequester
import com.derfence.astroface.wear.display.DisplayModeRepository

class ModeCycleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DisplayModeRepository.cycleNext(applicationContext)
        DialUpdateRequester.requestAll(applicationContext)
        finish()
    }
}
