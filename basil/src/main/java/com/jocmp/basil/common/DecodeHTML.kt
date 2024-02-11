

package com.jocmp.basil.common

import android.text.Html

@Suppress("DEPRECATION")
fun decodeHTML(text: String): String {
    return Html.fromHtml(text).toString()
}
