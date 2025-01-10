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

/**
 * Returns an HTML escaped representation of the given plain text.
 *
 * Copied from android.text.Html
 */
val String.escapingHTMLCharacters: String
    get() {
        val out = java.lang.StringBuilder()
        withinStyle(out, this, 0, this.length)
        return out.toString()
    }

private fun withinStyle(out: StringBuilder, text: CharSequence, start: Int, end: Int) {
    var i: Int = start
    while (i < end) {
        val c: Char = text.get(i)

        if (c == '<') {
            out.append("&lt;")
        } else if (c == '>') {
            out.append("&gt;")
        } else if (c == '&') {
            out.append("&amp;")
        } else if (c.code in 0xD800..0xDFFF) {
            if (c.code < 0xDC00 && i + 1 < end) {
                val d: Char = text.get(i + 1)
                if (d.code in 0xDC00..0xDFFF) {
                    i++
                    val codepoint = 0x010000 or (c.code - 0xD800 shl 10) or d.code - 0xDC00
                    out.append("&#").append(codepoint).append(";")
                }
            }
        } else if (c.code > 0x7E || c < ' ') {
            out.append("&#").append(c.code).append(";")
        } else if (c == ' ') {
            while (i + 1 < end && text.get(i + 1) == ' ') {
                out.append("&nbsp;")
                i++
            }

            out.append(' ')
        } else {
            out.append(c)
        }
        i++
    }
}
