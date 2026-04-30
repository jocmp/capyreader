package com.jocmp.capy.articles

enum class FontOption {
    SYSTEM_DEFAULT,
    ATKINSON_HYPERLEGIBLE,
    INTER,
    JOST,
    LITERATA,
    OPEN_DYSLEXIC,
    POPPINS,
    VOLLKORN;

    val slug: String
        get() = when(this) {
            SYSTEM_DEFAULT -> "default"
            ATKINSON_HYPERLEGIBLE -> "atkinson_hyperlegible"
            INTER -> "inter"
            JOST -> "jost"
            LITERATA -> "literata"
            OPEN_DYSLEXIC -> "opendyslexic"
            POPPINS -> "poppins"
            VOLLKORN -> "vollkorn"
        }

    companion object {
        val default = SYSTEM_DEFAULT
    }
}
