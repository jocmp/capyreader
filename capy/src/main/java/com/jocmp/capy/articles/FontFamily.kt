package com.jocmp.capy.articles

enum class FontFamily {
    SYSTEM_DEFAULT,
    POPPINS,
    ATKINSON_HYPERLEGIBLE,
    VOLLKORN;

    val slug: String
        get() = when(this) {
            SYSTEM_DEFAULT -> "default"
            POPPINS -> "poppins"
            ATKINSON_HYPERLEGIBLE -> "atkinson_hyperlegible"
            VOLLKORN -> "vollkorn"
        }

    companion object {
        val default = SYSTEM_DEFAULT

        val sorted: List<FontFamily>
            get() = listOf(
                SYSTEM_DEFAULT,
                POPPINS,
                ATKINSON_HYPERLEGIBLE,
                VOLLKORN
            )
    }
}
