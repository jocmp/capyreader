package com.jocmp.basil.opml

data class OPMLDocument(
    var title: String = "",
    val outlines: MutableList<Outline> = mutableListOf()
)
