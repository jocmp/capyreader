package com.jocmp.basil

import android.net.Uri
import android.text.TextUtils
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URLEncoder

@Serializable
data class Folder(
    val title: String,
    val feeds: MutableList<Feed> = mutableListOf(),
    override val count: Long = 0,
): Countable {
    val encodedTitle: String
        get() = TextUtils.htmlEncode(title)

    override fun equals(other: Any?): Boolean {
        if (other is Folder) {
            return title == other.title
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        val result = title.hashCode()
        return 31 * result
    }
}
