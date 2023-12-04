package com.jocmp.feedfinder.source

import com.jocmp.feedfinder.Feed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URI

internal interface Source {
    val document: Document?
    fun find(): List<Feed>
}

internal class BaseSource(url: URI) : Source {
    override val document: Document? by lazy {
        return@lazy try {
            Jsoup.connect(url.toString()).get()
        } catch (e: IOException) {
            null
        }
    }

    override fun find(): List<Feed> {
        return emptyList()
    }
}
