package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

fun isWordpress(doc: Doc): Boolean = doc(IS_WP_SELECTOR).length > 0
