package com.jocmp.mallet.tex

import java.math.BigDecimal
import java.math.RoundingMode

private val ptPerUnit =
    mapOf(
        "pt" to 1.0,
        "mm" to 7227.0 / 2540,
        "cm" to 7227.0 / 254,
        "in" to 72.27,
        "bp" to 803.0 / 800,
        "pc" to 12.0,
        "dd" to 1238.0 / 1157,
        "cc" to 14856.0 / 1157,
        "nd" to 685.0 / 642,
        "nc" to 1370.0 / 107,
        "sp" to 1.0 / 65536,
        "px" to 803.0 / 800,
    )

private val relativeUnits = setOf("ex", "em", "mu")

fun validUnit(unit: String): Boolean = unit in ptPerUnit || unit in relativeUnits

fun validUnit(measurement: Measurement): Boolean = validUnit(measurement.unit)

fun calculateSize(sizeValue: Measurement, options: Options): Double {
    val unitScale = ptPerUnit[sizeValue.unit]
    val scale =
        if (unitScale != null) {
            unitScale / options.fontMetrics().ptPerEm / options.sizeMultiplier
        } else if (sizeValue.unit == "mu") {
            options.fontMetrics().cssEmPerMu
        } else {
            val unitOptions = if (options.style.isTight()) options.havingStyle(options.style.text()) else options
            var scale =
                when (sizeValue.unit) {
                    "ex" -> unitOptions.fontMetrics().xHeight
                    "em" -> unitOptions.fontMetrics().quad
                    else -> throw ParseError("Invalid unit: '${sizeValue.unit}'")
                }
            if (unitOptions !== options) {
                scale *= unitOptions.sizeMultiplier / options.sizeMultiplier
            }
            scale
        }
    return minOf(sizeValue.number * scale, options.maxSize)
}

/** KaTeX's `+n.toFixed(4) + "em"`. */
fun makeEm(n: Double): String = "${roundEm(n)}em"

private fun roundEm(n: Double): String {
    if (!n.isFinite()) {
        return n.toString()
    }
    val rounded = BigDecimal(n).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
    return if (rounded.signum() == 0) "0" else rounded.toPlainString()
}
