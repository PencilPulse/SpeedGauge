package com.ease.speedgauge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.speedgauge.SpeedGaugeView
import com.example.speedgauge.model.SpeedRange
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var speedGauge: SpeedGaugeView
    private val handler = Handler(Looper.getMainLooper())
    private val updateSpeed = object : Runnable {
        override fun run() {
            val randomSpeed =  50f
            speedGauge.setSpeed(randomSpeed)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedGauge = findViewById(R.id.speedGauge)
        setupSpeedGauge()
        handler.post(updateSpeed)
    }

    private fun setupSpeedGauge() = with(speedGauge) {
        minValue = 0f
        maxValue = 100f  // Set max to 50 to match total range

        listOf(
            SpeedRange(0f, 1f, R.color.red),
            SpeedRange(1f, 5f, R.color.orange),
            SpeedRange(5f, 10f, R.color.yellow),
            SpeedRange(10f, 20f, R.color.light_green),
            SpeedRange(20f, 50f, R.color.green),
            SpeedRange(50f, 100f, R.color.green)
        ).let { addRanges(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSpeed)
    }
}