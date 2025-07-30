package com.example.speedgauge.model

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.speedgauge.SpeedGaugeSegment

data class SpeedRange(
    val from: Float,
    val to: Float,
    @ColorRes val colorRes: Int
)

fun SpeedRange.toSegment(context: Context, inactiveColor: Int = android.graphics.Color.LTGRAY): SpeedGaugeSegment {
    return SpeedGaugeSegment(
        startValue = from,
        endValue = to,
        inactiveColor = inactiveColor,
        activeColor = ContextCompat.getColor(context, colorRes)
    )
}
