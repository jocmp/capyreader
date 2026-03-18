package com.jocmp.capy

import com.jocmp.capy.common.withIOContext
import org.jsoup.nodes.Entities

class StarredBookmarksFile(
    private val account: Account,
) {
    suspend fun bookmarksDocument(): String = withIOContext {
        val entries = account.database.articlesQueries
            .starredExports()
            .executeAsList()

        buildString {
            appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
            appendLine("<!-- This is an automatically generated file. -->")
            appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
            appendLine("<TITLE>Starred Articles</TITLE>")
            appendLine("<H1>Starred Articles</H1>")
            appendLine("<DL><p>")
            for (entry in entries) {
                val title = Entities.escape(entry.title.orEmpty())
                val url = Entities.escape(entry.url.orEmpty())
                val addDate = entry.published_at ?: 0
                appendLine("    <DT><A HREF=\"$url\" ADD_DATE=\"$addDate\">$title</A>")
            }
            appendLine("</DL><p>")
        }
    }
}
