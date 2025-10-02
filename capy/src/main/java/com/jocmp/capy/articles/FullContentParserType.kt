package com.jocmp.capy.articles

enum class FullContentParserType {
    MERCURY_PARSER,
    DEFUDDLE;

    companion object {
        val default
            get() = MERCURY_PARSER
    }
}
