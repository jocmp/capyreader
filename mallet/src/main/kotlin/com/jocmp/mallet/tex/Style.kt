package com.jocmp.mallet.tex

/** TeX's eight styles: display, text, script, scriptscript, each normal or cramped. */
class Style private constructor(
    val id: Int,
    val size: Int,
    val cramped: Boolean,
) {
    fun sup(): Style = styles[supMap[id]]

    fun sub(): Style = styles[subMap[id]]

    fun fracNum(): Style = styles[fracNumMap[id]]

    fun fracDen(): Style = styles[fracDenMap[id]]

    fun cramp(): Style = styles[crampMap[id]]

    fun text(): Style = styles[textMap[id]]

    fun isTight(): Boolean = size >= 2

    companion object {
        private const val D = 0
        private const val DC = 1
        private const val T = 2
        private const val TC = 3
        private const val S = 4
        private const val SC = 5
        private const val SS = 6
        private const val SSC = 7

        private val styles =
            listOf(
                Style(D, 0, false),
                Style(DC, 0, true),
                Style(T, 1, false),
                Style(TC, 1, true),
                Style(S, 2, false),
                Style(SC, 2, true),
                Style(SS, 3, false),
                Style(SSC, 3, true),
            )

        private val supMap = intArrayOf(S, SC, S, SC, SS, SSC, SS, SSC)
        private val subMap = intArrayOf(SC, SC, SC, SC, SSC, SSC, SSC, SSC)
        private val fracNumMap = intArrayOf(T, TC, S, SC, SS, SSC, SS, SSC)
        private val fracDenMap = intArrayOf(TC, TC, SC, SC, SSC, SSC, SSC, SSC)
        private val crampMap = intArrayOf(DC, DC, TC, TC, SC, SC, SSC, SSC)
        private val textMap = intArrayOf(D, DC, T, TC, T, TC, T, TC)

        val DISPLAY: Style = styles[D]
        val TEXT: Style = styles[T]
        val SCRIPT: Style = styles[S]
        val SCRIPTSCRIPT: Style = styles[SS]
    }
}
