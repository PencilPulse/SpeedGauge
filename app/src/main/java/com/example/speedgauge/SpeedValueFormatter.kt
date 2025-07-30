interface SpeedValueFormatter {
    fun format(value: Float): String
}

class DefaultSpeedFormatter : SpeedValueFormatter {
    override fun format(value: Float): String = "%.0f".format(value)
}
