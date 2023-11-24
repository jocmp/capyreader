package com.jocmp.basil.opml

internal data class OPMLDocument(
    val outlines: MutableList<Outline> = mutableListOf()
)
