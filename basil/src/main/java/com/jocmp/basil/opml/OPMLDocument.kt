package com.jocmp.basil.opml

data class OPMLDocument(
    val outlines: MutableList<Outline> = mutableListOf()
)
