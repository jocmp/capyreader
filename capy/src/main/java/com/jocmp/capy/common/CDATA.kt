package com.jocmp.capy.common

private val CDATA_REGEX = Regex("<!\\[CDATA\\[(.*?)]]>", RegexOption.DOT_MATCHES_ALL)

internal fun String.unwrapCDATA(): String {
    return CDATA_REGEX.replace(this) { it.groupValues[1] }
}
