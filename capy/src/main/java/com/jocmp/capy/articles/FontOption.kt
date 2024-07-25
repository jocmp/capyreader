package com.jocmp.capy.articles

enum class FontOption {
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

        val sorted: List<FontOption>
            get() = listOf(
                SYSTEM_DEFAULT,
                POPPINS,
                ATKINSON_HYPERLEGIBLE,
                VOLLKORN
            )
    }
}
