package com.derfence.astroface.wear.dial

import android.graphics.Canvas

data class DialViewport(
    val originX: Int,
    val originY: Int,
    val width: Int,
    val height: Int
) {
    fun translate(canvas: Canvas) {
        canvas.translate(-originX.toFloat(), -originY.toFloat())
    }

    companion object {
        val CELESTIAL = DialViewport(originX = 55, originY = 55, width = 340, height = 340)
        val STATUS = DialViewport(originX = 125, originY = 120, width = 200, height = 190)
    }
}
