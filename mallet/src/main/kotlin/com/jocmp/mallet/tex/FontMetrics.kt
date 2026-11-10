package com.jocmp.mallet.tex

data class CharacterMetrics(
    val depth: Double,
    val height: Double,
    val italic: Double,
    val skew: Double,
    val width: Double,
)

/** TeX's sigma and xi font parameters at one of three sizes (text, script, scriptscript). */
class FontMetrics(sizeIndex: Int) {
    val slant = sigmasAndXis.getValue("slant")[sizeIndex]
    val space = sigmasAndXis.getValue("space")[sizeIndex]
    val stretch = sigmasAndXis.getValue("stretch")[sizeIndex]
    val shrink = sigmasAndXis.getValue("shrink")[sizeIndex]
    val xHeight = sigmasAndXis.getValue("xHeight")[sizeIndex]
    val quad = sigmasAndXis.getValue("quad")[sizeIndex]
    val extraSpace = sigmasAndXis.getValue("extraSpace")[sizeIndex]
    val num1 = sigmasAndXis.getValue("num1")[sizeIndex]
    val num2 = sigmasAndXis.getValue("num2")[sizeIndex]
    val num3 = sigmasAndXis.getValue("num3")[sizeIndex]
    val denom1 = sigmasAndXis.getValue("denom1")[sizeIndex]
    val denom2 = sigmasAndXis.getValue("denom2")[sizeIndex]
    val sup1 = sigmasAndXis.getValue("sup1")[sizeIndex]
    val sup2 = sigmasAndXis.getValue("sup2")[sizeIndex]
    val sup3 = sigmasAndXis.getValue("sup3")[sizeIndex]
    val sub1 = sigmasAndXis.getValue("sub1")[sizeIndex]
    val sub2 = sigmasAndXis.getValue("sub2")[sizeIndex]
    val supDrop = sigmasAndXis.getValue("supDrop")[sizeIndex]
    val subDrop = sigmasAndXis.getValue("subDrop")[sizeIndex]
    val delim1 = sigmasAndXis.getValue("delim1")[sizeIndex]
    val delim2 = sigmasAndXis.getValue("delim2")[sizeIndex]
    val axisHeight = sigmasAndXis.getValue("axisHeight")[sizeIndex]
    val defaultRuleThickness = sigmasAndXis.getValue("defaultRuleThickness")[sizeIndex]
    val bigOpSpacing1 = sigmasAndXis.getValue("bigOpSpacing1")[sizeIndex]
    val bigOpSpacing2 = sigmasAndXis.getValue("bigOpSpacing2")[sizeIndex]
    val bigOpSpacing3 = sigmasAndXis.getValue("bigOpSpacing3")[sizeIndex]
    val bigOpSpacing4 = sigmasAndXis.getValue("bigOpSpacing4")[sizeIndex]
    val bigOpSpacing5 = sigmasAndXis.getValue("bigOpSpacing5")[sizeIndex]
    val sqrtRuleThickness = sigmasAndXis.getValue("sqrtRuleThickness")[sizeIndex]
    val ptPerEm = sigmasAndXis.getValue("ptPerEm")[sizeIndex]
    val doubleRuleSep = sigmasAndXis.getValue("doubleRuleSep")[sizeIndex]
    val arrayRuleWidth = sigmasAndXis.getValue("arrayRuleWidth")[sizeIndex]
    val fboxsep = sigmasAndXis.getValue("fboxsep")[sizeIndex]
    val fboxrule = sigmasAndXis.getValue("fboxrule")[sizeIndex]
    val cssEmPerMu = quad / 18
}

private val sigmasAndXis =
    mapOf(
        "slant" to doubleArrayOf(0.250, 0.250, 0.250),
        "space" to doubleArrayOf(0.000, 0.000, 0.000),
        "stretch" to doubleArrayOf(0.000, 0.000, 0.000),
        "shrink" to doubleArrayOf(0.000, 0.000, 0.000),
        "xHeight" to doubleArrayOf(0.431, 0.431, 0.431),
        "quad" to doubleArrayOf(1.000, 1.171, 1.472),
        "extraSpace" to doubleArrayOf(0.000, 0.000, 0.000),
        "num1" to doubleArrayOf(0.677, 0.732, 0.925),
        "num2" to doubleArrayOf(0.394, 0.384, 0.387),
        "num3" to doubleArrayOf(0.444, 0.471, 0.504),
        "denom1" to doubleArrayOf(0.686, 0.752, 1.025),
        "denom2" to doubleArrayOf(0.345, 0.344, 0.532),
        "sup1" to doubleArrayOf(0.413, 0.503, 0.504),
        "sup2" to doubleArrayOf(0.363, 0.431, 0.404),
        "sup3" to doubleArrayOf(0.289, 0.286, 0.294),
        "sub1" to doubleArrayOf(0.150, 0.143, 0.200),
        "sub2" to doubleArrayOf(0.247, 0.286, 0.400),
        "supDrop" to doubleArrayOf(0.386, 0.353, 0.494),
        "subDrop" to doubleArrayOf(0.050, 0.071, 0.100),
        "delim1" to doubleArrayOf(2.390, 1.700, 1.980),
        "delim2" to doubleArrayOf(1.010, 1.157, 1.420),
        "axisHeight" to doubleArrayOf(0.250, 0.250, 0.250),
        "defaultRuleThickness" to doubleArrayOf(0.04, 0.049, 0.049),
        "bigOpSpacing1" to doubleArrayOf(0.111, 0.111, 0.111),
        "bigOpSpacing2" to doubleArrayOf(0.166, 0.166, 0.166),
        "bigOpSpacing3" to doubleArrayOf(0.2, 0.2, 0.2),
        "bigOpSpacing4" to doubleArrayOf(0.6, 0.611, 0.611),
        "bigOpSpacing5" to doubleArrayOf(0.1, 0.143, 0.143),
        "sqrtRuleThickness" to doubleArrayOf(0.04, 0.04, 0.04),
        "ptPerEm" to doubleArrayOf(10.0, 10.0, 10.0),
        "doubleRuleSep" to doubleArrayOf(0.2, 0.2, 0.2),
        "arrayRuleWidth" to doubleArrayOf(0.04, 0.04, 0.04),
        "fboxsep" to doubleArrayOf(0.3, 0.3, 0.3),
        "fboxrule" to doubleArrayOf(0.04, 0.04, 0.04),
    )

private val extraCharacterMap =
    mapOf(
        'Å' to 'A', 'Ð' to 'D', 'Þ' to 'o', 'å' to 'a', 'ð' to 'd', 'þ' to 'o',
        'А' to 'A', 'Б' to 'B', 'В' to 'B', 'Г' to 'F', 'Д' to 'A', 'Е' to 'E', 'Ж' to 'K', 'З' to '3',
        'И' to 'N', 'Й' to 'N', 'К' to 'K', 'Л' to 'N', 'М' to 'M', 'Н' to 'H', 'О' to 'O', 'П' to 'N',
        'Р' to 'P', 'С' to 'C', 'Т' to 'T', 'У' to 'y', 'Ф' to 'O', 'Х' to 'X', 'Ц' to 'U', 'Ч' to 'h',
        'Ш' to 'W', 'Щ' to 'W', 'Ъ' to 'B', 'Ы' to 'X', 'Ь' to 'B', 'Э' to '3', 'Ю' to 'X', 'Я' to 'R',
        'а' to 'a', 'б' to 'b', 'в' to 'a', 'г' to 'r', 'д' to 'y', 'е' to 'e', 'ж' to 'm', 'з' to 'e',
        'и' to 'n', 'й' to 'n', 'к' to 'n', 'л' to 'n', 'м' to 'm', 'н' to 'n', 'о' to 'o', 'п' to 'n',
        'р' to 'p', 'с' to 'c', 'т' to 'o', 'у' to 'y', 'ф' to 'b', 'х' to 'x', 'ц' to 'n', 'ч' to 'n',
        'ш' to 'w', 'щ' to 'w', 'ъ' to 'a', 'ы' to 'm', 'ь' to 'a', 'э' to 'e', 'ю' to 'm', 'я' to 'r',
    )

private val metricMap: Map<String, Map<Int, CharacterMetrics>> by lazy { loadMetricMap() }

private val globalMetrics = arrayOfNulls<FontMetrics>(3)

/** Metrics keyed like KaTeX's: by the first UTF-16 code unit of [character]. */
fun getCharacterMetrics(character: String, font: String, mode: Mode): CharacterMetrics? {
    val fontMetrics = metricMap[font] ?: throw IllegalStateException("Font metrics not found for font: $font.")
    val first = character.firstOrNull() ?: return null
    var ch = first.code
    var metrics = fontMetrics[ch]
    if (metrics == null) {
        extraCharacterMap[first]?.let { replacement ->
            ch = replacement.code
            metrics = fontMetrics[ch]
        }
    }
    if (metrics == null && mode == Mode.TEXT && supportedCodepoint(ch)) {
        metrics = fontMetrics['M'.code]
    }
    return metrics
}

fun getGlobalMetrics(size: Int): FontMetrics {
    val sizeIndex =
        when {
            size >= 5 -> 0
            size >= 3 -> 1
            else -> 2
        }
    return globalMetrics[sizeIndex] ?: FontMetrics(sizeIndex).also { globalMetrics[sizeIndex] = it }
}

private fun loadMetricMap(): Map<String, Map<Int, CharacterMetrics>> {
    val stream =
        FontMetrics::class.java.getResourceAsStream("/com/jocmp/mallet/tex/fontMetricsData.tsv")
            ?: throw IllegalStateException("Missing fontMetricsData.tsv")
    val result = HashMap<String, HashMap<Int, CharacterMetrics>>()
    stream.bufferedReader().useLines { lines ->
        lines.filter { it.isNotBlank() }.forEach { line ->
            val fields = line.split('\t')
            val metrics =
                CharacterMetrics(
                    depth = fields[2].toDouble(),
                    height = fields[3].toDouble(),
                    italic = fields[4].toDouble(),
                    skew = fields[5].toDouble(),
                    width = fields[6].toDouble(),
                )
            result.getOrPut(fields[0]) { HashMap() }[fields[1].toInt()] = metrics
        }
    }
    return result
}
