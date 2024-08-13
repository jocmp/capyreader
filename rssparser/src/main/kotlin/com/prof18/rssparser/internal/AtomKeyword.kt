package com.prof18.rssparser.internal

internal sealed class AtomKeyword(val value: String) {
    data object Atom : AtomKeyword("feed")
    data object Title : AtomKeyword("title")
    data object Icon: AtomKeyword("icon")
    data object Link : AtomKeyword("link") {
        data object Href : AtomKeyword("href")
        data object Rel : AtomKeyword("rel")
        data object Edit : AtomKeyword("edit")
        data object Self : AtomKeyword("self")
    }
    data object Subtitle : AtomKeyword("subtitle")
    data object Updated: AtomKeyword("updated")
    data object Entry {
        data object Item : AtomKeyword("entry")
        data object Guid : AtomKeyword("id")
        data object Content : AtomKeyword("content")
        data object Published : AtomKeyword("published")
        data object Category : AtomKeyword("category")
        data object Term : AtomKeyword("term")
        data object Description : AtomKeyword("summary")
        data object Author : AtomKeyword("name")
        data object Email : AtomKeyword("email")
    }
}
