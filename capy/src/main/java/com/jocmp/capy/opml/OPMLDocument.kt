package com.jocmp.capy.opml

internal data class OPMLDocument(
    val outlines: MutableList<Outline> = mutableListOf()
)
