package com.jocmp.mallet.tex

/**
 * The subset of KaTeX's settings that affect parsing and layout. `strict` is fixed at KaTeX's
 * default of "warn", which only logs, so strict checks never change behavior.
 */
class Settings(
    val displayMode: Boolean = false,
    val leqno: Boolean = false,
    val fleqn: Boolean = false,
    val throwOnError: Boolean = true,
    val errorColor: String = "#cc0000",
    val macros: MutableMap<String, MacroDefinition> = mutableMapOf(),
    val minRuleThickness: Double = 0.0,
    val colorIsTextColor: Boolean = false,
    val maxSize: Double = Double.POSITIVE_INFINITY,
    val maxExpand: Int = 1000,
    val globalGroup: Boolean = false,
) {
    @Suppress("UNUSED_PARAMETER")
    fun reportNonstrict(errorCode: String, errorMsg: String, token: Located? = null) = Unit

    @Suppress("UNUSED_PARAMETER")
    fun useStrictBehavior(errorCode: String, errorMsg: String, token: Located? = null): Boolean = false

    /** KaTeX's default `trust: false`: feeds may not link or embed through math. */
    @Suppress("UNUSED_PARAMETER")
    fun isTrusted(command: String, url: String? = null): Boolean = false
}
