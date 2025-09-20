package com.jocmp.capy.articles

enum class FullContentParserType {
    DEFUDDLE,
    MERCURY_PARSER;

    companion object {
        val default
            get() = DEFUDDLE
    }
}
