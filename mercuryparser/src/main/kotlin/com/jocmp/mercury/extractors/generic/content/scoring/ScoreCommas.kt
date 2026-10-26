package com.jocmp.mercury.extractors.generic.content.scoring

// return 1 for every comma in text
fun scoreCommas(text: String): Int = text.count { it == ',' }
