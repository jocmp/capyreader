package com.capyreader.app.preferences

import com.capyreader.app.R
import com.jocmp.capy.articles.FullContentParserType
import com.jocmp.capy.articles.FullContentParserType.DEFUDDLE
import com.jocmp.capy.articles.FullContentParserType.MERCURY_PARSER

val FullContentParserType.translationKey: Int
    get() = when (this) {
        DEFUDDLE -> R.string.full_content_parser_defuddle
        MERCURY_PARSER -> R.string.full_content_parser_mercury_parser
    }

