package com.jocmp.capy.articles

enum class FontOption {
    SYSTEM_DEFAULT,
    ATKINSON_HYPERLEGIBLE,
    INTER,
    JOST,
    LITERATA,
    POPPINS,
    VOLLKORN;

    val slug: String
        get() = when(this) {
            SYSTEM_DEFAULT -> "default"
            ATKINSON_HYPERLEGIBLE -> "atkinson_hyperlegible"
            INTER -> "inter"
            JOST -> "jost"
            LITERATA -> "literata"
            POPPINS -> "poppins"
            VOLLKORN -> "vollkorn"
        }

    companion object {
        val default = SYSTEM_DEFAULT
    }
}
