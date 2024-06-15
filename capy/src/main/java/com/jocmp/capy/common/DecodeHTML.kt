

package com.jocmp.capy.common

import android.text.Html

@Suppress("DEPRECATION")
fun decodeHTML(text: String): String {
    return Html.fromHtml(text).toString()
}
