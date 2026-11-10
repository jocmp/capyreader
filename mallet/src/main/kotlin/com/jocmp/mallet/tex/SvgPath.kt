package com.jocmp.mallet.tex

/** Receives absolute path commands. */
interface PathSink {
    fun moveTo(x: Double, y: Double)

    fun lineTo(x: Double, y: Double)

    fun cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x: Double, y: Double)

    fun quadTo(x1: Double, y1: Double, x: Double, y: Double)

    fun close()
}

/** Parses SVG path data (M, L, H, V, C, S, Q, T, Z in both cases) into absolute commands. */
object SvgPath {
    private val tokenRegex = Regex("[MmLlHhVvCcSsQqTtZzAa]|[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?")

    fun parse(data: String, sink: PathSink) {
        val tokens = tokenRegex.findAll(data).map { it.value }.toList()
        var index = 0
        var command = 'M'
        var x = 0.0
        var y = 0.0
        var startX = 0.0
        var startY = 0.0
        var controlX = 0.0
        var controlY = 0.0
        var previous = ' '

        fun number(): Double {
            val value = tokens[index].toDouble()
            index += 1
            return value
        }

        while (index < tokens.size) {
            val token = tokens[index]
            if (token.length == 1 && token[0].isLetter()) {
                command = token[0]
                index += 1
                if (command == 'Z' || command == 'z') {
                    sink.close()
                    x = startX
                    y = startY
                    previous = command
                    continue
                }
            }
            val relative = command.isLowerCase()
            val ox = if (relative) x else 0.0
            val oy = if (relative) y else 0.0
            when (command.uppercaseChar()) {
                'M' -> {
                    x = ox + number()
                    y = oy + number()
                    startX = x
                    startY = y
                    sink.moveTo(x, y)
                    command = if (relative) 'l' else 'L'
                }
                'L' -> {
                    x = ox + number()
                    y = oy + number()
                    sink.lineTo(x, y)
                }
                'H' -> {
                    x = ox + number()
                    sink.lineTo(x, y)
                }
                'V' -> {
                    y = oy + number()
                    sink.lineTo(x, y)
                }
                'C' -> {
                    val x1 = ox + number()
                    val y1 = oy + number()
                    controlX = ox + number()
                    controlY = oy + number()
                    x = ox + number()
                    y = oy + number()
                    sink.cubicTo(x1, y1, controlX, controlY, x, y)
                }
                'S' -> {
                    val reflect = previous.uppercaseChar() == 'C' || previous.uppercaseChar() == 'S'
                    val x1 = if (reflect) 2 * x - controlX else x
                    val y1 = if (reflect) 2 * y - controlY else y
                    controlX = ox + number()
                    controlY = oy + number()
                    x = ox + number()
                    y = oy + number()
                    sink.cubicTo(x1, y1, controlX, controlY, x, y)
                }
                'Q' -> {
                    controlX = ox + number()
                    controlY = oy + number()
                    x = ox + number()
                    y = oy + number()
                    sink.quadTo(controlX, controlY, x, y)
                }
                'T' -> {
                    val reflect = previous.uppercaseChar() == 'Q' || previous.uppercaseChar() == 'T'
                    controlX = if (reflect) 2 * x - controlX else x
                    controlY = if (reflect) 2 * y - controlY else y
                    x = ox + number()
                    y = oy + number()
                    sink.quadTo(controlX, controlY, x, y)
                }
                else -> throw IllegalArgumentException("Unsupported path command $command")
            }
            previous = command
        }
    }
}

/** The viewBox-to-viewport mapping SVG applies for [TexItem.Svg]'s preserveAspectRatio. */
class SvgTransform(
    val scaleX: Double,
    val scaleY: Double,
    val translateX: Double,
    val translateY: Double,
)

fun TexItem.Svg.transform(): SvgTransform {
    val box = viewBox ?: return SvgTransform(1000.0.let { 1 / it }, 1 / 1000.0, x, y)
    val (minX, minY, boxWidth, boxHeight) = box
    var scaleX = width / boxWidth
    var scaleY = height / boxHeight
    val ratio = preserveAspectRatio?.trim()?.split(Regex("\\s+")).orEmpty()
    val align = ratio.getOrNull(0) ?: "xMidYMid"
    if (align != "none") {
        val slice = ratio.getOrNull(1) == "slice"
        val uniform = if (slice) maxOf(scaleX, scaleY) else minOf(scaleX, scaleY)
        scaleX = uniform
        scaleY = uniform
    }
    var translateX = x - minX * scaleX
    var translateY = y - minY * scaleY
    if (align != "none") {
        val extraX = width - boxWidth * scaleX
        val extraY = height - boxHeight * scaleY
        translateX +=
            when {
                align.startsWith("xMid") -> extraX / 2
                align.startsWith("xMax") -> extraX
                else -> 0.0
            }
        translateY +=
            when {
                align.endsWith("YMid") -> extraY / 2
                align.endsWith("YMax") -> extraY
                else -> 0.0
            }
    }
    return SvgTransform(scaleX, scaleY, translateX, translateY)
}
