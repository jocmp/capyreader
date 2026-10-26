package com.jocmp.mercury.cleaners

import com.jocmp.mercury.utils.text.normalizeSpaces

// Take an author string (like 'By David Smith ') and clean it to
// just the name(s): 'David Smith'.
fun cleanAuthor(author: String): String = normalizeSpaces(author.replace(CLEAN_AUTHOR_RE, "$2").trim())
