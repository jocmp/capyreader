package com.jocmp.mercury.utils.text

fun removeAnchor(url: String): String = url.split("#")[0].replace(Regex("/$"), "")
