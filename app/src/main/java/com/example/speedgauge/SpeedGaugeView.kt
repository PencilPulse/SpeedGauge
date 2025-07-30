package com.example.speedgauge

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.max
import kotlin.math.min
import com.ease.speedgauge.R
import com.example.speedgauge.model.SpeedRange
import com.example.speedgauge.model.toSegment

class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var segments = mutableListOf<SpeedGaugeSegment>()
    private var currentSpeed = 0f
    private val rect = RectF()
    private var maxSpeed = 100f
    private var speedAnimator: ValueAnimator? = null
    
    // Configurable properties
    var gaugeStrokeWidth = 50f
    var showText = true
    var animationDuration = 300L
    var valueFormatter: (Float) -> String = { "%.0f".format(it) }
    
    var minValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    
    var maxValue: Float = 100f
        set(value) {
            field = value
            invalidate()
        }

    init {
        setupAttributes(attrs, defStyleAttr)
        setupInitialState()
    }

    private fun setupAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SpeedGaugeView,
            defStyleAttr,
            0
        ).apply {
            try {
                gaugeStrokeWidth = getDimension(R.styleable.SpeedGaugeView_gaugeStrokeWidth, 40f)
                textPaint.textSize = getDimension(R.styleable.SpeedGaugeView_textSize, 12 * resources.displayMetrics.density)
                textPaint.color = getColor(R.styleable.SpeedGaugeView_textColor, Color.BLACK)
                showText = getBoolean(R.styleable.SpeedGaugeView_showText, true)
                animationDuration = getInteger(R.styleable.SpeedGaugeView_animationDuration, 300).toLong()
            } finally {
                recycle()
            }
        }
    }

    private fun setupInitialState() {
        textPaint.apply {
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        
        // Default segments with both active and inactive colors
        setSegments(listOf(
            SpeedGaugeSegment(0f, 5f, Color.LTGRAY, Color.RED),
            SpeedGaugeSegment(5f, 10f, Color.LTGRAY, Color.parseColor("#FF8C00")), // Dark Orange
            SpeedGaugeSegment(10f, 20f, Color.LTGRAY, Color.YELLOW),
            SpeedGaugeSegment(20f, 30f, Color.LTGRAY, Color.parseColor("#90EE90")), // Light Green
            SpeedGaugeSegment(30f, 50f, Color.LTGRAY, Color.GREEN),
            SpeedGaugeSegment(50f, 100f, Color.LTGRAY, Color.BLUE)
        ))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        
        val desiredWidth = max(minWidth, 200)
        val desiredHeight = max(minHeight, 150)
        
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }

    fun setSegments(newSegments: List<SpeedGaugeSegment>) {
        if (newSegments.isEmpty()) {
            throw IllegalArgumentException("Segments list cannot be empty")
        }
        
        // Verify segments are continuous and increasing
        for (i in 1 until newSegments.size) {
            if (newSegments[i].startValue != newSegments[i-1].endValue) {
                throw IllegalArgumentException("Segments must be continuous")
            }
            if (newSegments[i].startValue >= newSegments[i].endValue) {
                throw IllegalArgumentException("Segment values must be increasing")
            }
        }
        
        segments.clear()
        segments.addAll(newSegments)
        maxSpeed = newSegments.last().endValue
        invalidate()
    }

    fun setSpeed(speed: Float, animate: Boolean = true) {
        speedAnimator?.cancel()
        
        val targetSpeed = speed.coerceIn(0f, maxSpeed)
        if (animate) {
            speedAnimator = ValueAnimator.ofFloat(currentSpeed, targetSpeed).apply {
                duration = animationDuration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    currentSpeed = animator.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            currentSpeed = targetSpeed
            invalidate()
        }
    }

    private fun getCurrentArcColor(): Int {
        val progress = currentSpeed / maxSpeed
        val segmentIndex = (progress * segments.size).toInt().coerceIn(0, segments.size - 1)
        return segments[segmentIndex].activeColor
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        
        val centerX = width / 2f
        val centerY = height * 0.8f
        val radius = min(width, height) * 0.4f
        
        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        
        val totalAngle = 180f
        val needleAngle = totalAngle * (currentSpeed / maxSpeed)
        
        // First draw all segments in inactive color
        segments.forEachIndexed { index, segment ->
            paint.apply {
                color = segment.inactiveColor
                style = Paint.Style.STROKE
                strokeWidth = gaugeStrokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            
            val segmentStartAngle = 180f + (index * (totalAngle / segments.size))
            val sweepAngle = totalAngle / segments.size
            canvas.drawArc(rect, segmentStartAngle, sweepAngle, false, paint)
        }
        
        // Then draw the active portion up to the needle position
        segments.forEachIndexed { index, segment ->
            val segmentStartAngle = 180f + (index * (totalAngle / segments.size))
            val sweepAngle = totalAngle / segments.size
            val segmentEndAngle = segmentStartAngle + sweepAngle
            
            if (needleAngle >= segmentStartAngle - 180f) {
                paint.apply {
                    color = segment.activeColor
                    style = Paint.Style.STROKE
                    strokeWidth = gaugeStrokeWidth
                    strokeCap = Paint.Cap.ROUND
                }
                
                val activeEndAngle = min(segmentEndAngle, needleAngle + 180f)
                val activeSweepAngle = activeEndAngle - segmentStartAngle
                
                if (activeSweepAngle > 0) {
                    canvas.drawArc(rect, segmentStartAngle, activeSweepAngle, false, paint)
                }
            }
        }
        
        // Draw text labels
        if (showText) {
            val sweepAnglePerSegment = totalAngle / segments.size
            segments.forEachIndexed { index, segment ->
                val startAngle = 180f + (index * sweepAnglePerSegment)
                val textAngle = Math.toRadians(startAngle.toDouble())
                // Add extra padding for edge values
                val textRadius = radius + gaugeStrokeWidth + 
                    (if (index == 0 || index == segments.size - 1) gaugeStrokeWidth * 0.5f else 0f)
                val textX = (centerX + textRadius * Math.cos(textAngle)).toFloat()
                val textY = (centerY + textRadius * Math.sin(textAngle)).toFloat()
                
                // Adjust text alignment and position for edge cases
                textPaint.textAlign = when {
                    startAngle <= 185f -> Paint.Align.LEFT  // First value
                    startAngle >= 355f -> Paint.Align.RIGHT // Last value
                    else -> Paint.Align.CENTER
                }
                
                canvas.drawText(valueFormatter(segment.startValue), textX, textY, textPaint)
            }
            
            // Draw max value with more padding and adjusted angle
            textPaint.textAlign = Paint.Align.RIGHT
            val lastTextAngle = Math.toRadians(355.0) // Moved slightly to the left
            val lastTextRadius = radius + gaugeStrokeWidth * 2f // Increased padding
            val lastTextX = (centerX + lastTextRadius * Math.cos(lastTextAngle)).toFloat()
            val lastTextY = (centerY + lastTextRadius * Math.sin(lastTextAngle)).toFloat()
            canvas.drawText(valueFormatter(maxSpeed), lastTextX, lastTextY, textPaint)
        }

        // Draw the needle with matching arc color
        val currentArcColor = getCurrentArcColor()
        paint.apply {
            color = currentArcColor
            strokeWidth = 5f
            style = Paint.Style.FILL_AND_STROKE
        }
        
        val needleX = centerX + (radius - gaugeStrokeWidth/2) * 
            Math.cos(Math.toRadians(180.0 + needleAngle)).toFloat()
        val needleY = centerY + (radius - gaugeStrokeWidth/2) * 
            Math.sin(Math.toRadians(180.0 + needleAngle)).toFloat()
        
        canvas.drawLine(centerX, centerY, needleX, needleY, paint)
        
        // Draw center circle with same color
        paint.style = Paint.Style.FILL
        paint.color = currentArcColor
        canvas.drawCircle(centerX, centerY, 20f, paint)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable("superState", superState)
            putFloat("currentSpeed", currentSpeed)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            currentSpeed = state.getFloat("currentSpeed")
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        speedAnimator?.cancel()
    }

    fun addRanges(ranges: List<SpeedRange>, defaultInactiveColor: Int = Color.LTGRAY) {
        setSegments(ranges.map { it.toSegment(context, defaultInactiveColor) })
    }

    fun addRange(range: SpeedRange, defaultInactiveColor: Int = Color.LTGRAY) {
        segments.add(range.toSegment(context, defaultInactiveColor))
        invalidate()
    }
}
