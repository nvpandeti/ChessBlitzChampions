package com.nvp293.cbc

import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class Timer(private var TV: TextView) {
    private var endMillis = 0L // Only read, not Atomic

    fun millisLeft(): Long {return endMillis - System.currentTimeMillis()}

    suspend fun timerCo(durationMillis: Long) {
        endMillis = System.currentTimeMillis() + durationMillis
        var currentMillis = System.currentTimeMillis()
        // End color of replayButton is  red
        val delayMillis = 100L // Time step for updates
        // XML button
        TV.setBackgroundColor(Color.WHITE)

        while (coroutineContext.isActive
            && (endMillis > currentMillis)) {
            // XML TextView
            var minutes = (endMillis - currentMillis) / 60000L
            var seconds = (endMillis - currentMillis) - minutes * 60000
            TV.text = String.format(
                "%01d:%02.1f",
                minutes,
                seconds / 1000.0f
            )
            val scaleFactor = (endMillis - currentMillis).toFloat() / durationMillis.toFloat()
            TV.setBackgroundColor(
                Color.rgb(
                    255,
                    (255 * scaleFactor).toInt(),
                    (255 * scaleFactor).toInt()
                )
            )
            delay(delayMillis)
            currentMillis = System.currentTimeMillis()
        }
        TV.text = "0:00.0"
    }
}