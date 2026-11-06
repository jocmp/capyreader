package com.jocmp.malletmath

/**
 * TeX's inter-atom spacing table (The TeXbook, chapter 18).
 */
internal object MathSpacing {
    enum class Space {
        NONE,
        THIN,

        // NS: no space in script and scriptscript styles.
        NS_THIN,
        NS_MEDIUM,
        NS_THICK,
    }

    fun spaceBetween(left: AtomType, right: AtomType): Space {
        val row = index(left, isLeft = true)
        val column = index(right, isLeft = false)
        return table[row][column] ?: Space.NONE
    }

    private fun index(type: AtomType, isLeft: Boolean): Int =
        when (type) {
            AtomType.LARGE_OPERATOR -> 1
            AtomType.BINARY_OPERATOR -> 2
            AtomType.RELATION -> 3
            AtomType.OPEN -> 4
            AtomType.CLOSE -> 5
            AtomType.PUNCTUATION -> 6
            AtomType.FRACTION, AtomType.INNER -> 7
            // Radicals space as ordinaries on their right, but keep a gap before an ordinary so
            // \sqrt{4}4 doesn't run together.
            AtomType.RADICAL -> if (isLeft) 8 else 0
            else -> 0
        }

    private val N = Space.NONE
    private val T = Space.THIN
    private val S = Space.NS_THIN
    private val M = Space.NS_MEDIUM
    private val K = Space.NS_THICK

    // Null marks pairs TeX rules out; preprocessing should prevent them, so they fall back to none.
    private val table: Array<Array<Space?>> =
        arrayOf(
            // ord, op, bin, rel, open, close, punct, inner
            arrayOf(N, T, M, K, N, N, N, S), // ordinary
            arrayOf(T, T, null, K, N, N, N, S), // operator
            arrayOf(M, M, null, null, M, null, null, M), // binary
            arrayOf(K, K, null, N, K, N, N, K), // relation
            arrayOf(N, N, null, N, N, N, N, N), // open
            arrayOf(N, T, M, K, N, N, N, S), // close
            arrayOf(S, S, null, S, S, S, S, S), // punct
            arrayOf(S, T, M, K, S, N, S, S), // inner / fraction
            arrayOf(M, S, M, K, N, N, N, S), // radical
        )
}
