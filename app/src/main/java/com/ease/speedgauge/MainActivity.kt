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
            val randomSpeed = Random.nextFloat() * 100
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
        maxValue = 100f

        listOf(
            SpeedRange(0f, 5f, R.color.red),
            SpeedRange(5f, 10f, R.color.orange),
            SpeedRange(10f, 20f, R.color.yellow),
            SpeedRange(20f, 30f, R.color.light_green),
            SpeedRange(30f, 50f, R.color.green)
        ).let { addRanges(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSpeed)
    }
}