package com.jocmp.basil

import android.net.Uri
import android.text.TextUtils
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URLEncoder

@Serializable
data class Folder(
    val title: String,
    val feeds: List<Feed> = emptyList(),
    override val count: Long = 0,
) : Countable
