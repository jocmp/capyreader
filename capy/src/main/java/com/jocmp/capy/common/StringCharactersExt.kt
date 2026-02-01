package com.jocmp.capy.common

/**
 * Returns the string with the special XML characters (other than single-quote) ampersand-escaped.
 *
 * The four escaped characters are `<`, `>`, `&`, and `"`.
 */
val String.escapingSpecialXMLCharacters: String
    get() {
        var escaped = ""

        this.toCharArray().forEach { char ->
            when (char) {
                '&' -> escaped += "&amp;"
                '<' -> escaped += "&lt;"
                '>' -> escaped += "&gt;"
                '\"' -> escaped += "&quot;"
                else -> escaped += char
            }
        }

        return escaped
    }

val String.unescapingHTMLCharacters: String
    get() {
        return this
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

val String.escapingSpecialHTMLCharacters: String
    get() = replace("&", "&amp;")
