package com.prof18.rssparser.internal

/**
 * RDF Site Summary 1.0 Modules
 *
 * - [Slash](https://web.resource.org/rss/1.0/modules/slash/)
 * - [Dublin Core](https://web.resource.org/rss/1.0/modules/dc/)
 */
internal sealed class RdfKeyword(val value: String) {
    data object Rdf: RdfKeyword("rdf:RDF")
    data object Channel: RdfKeyword("channel") {
        data object Title: RdfKeyword("title")
        data object Description: RdfKeyword("description")
        data object Image {
            data object Tag:  RdfKeyword("image")
            data object ResourceAttribute:  RdfKeyword("rdf:resource")
        }
        data object Link: RdfKeyword("link")
        data object DCDate: RdfKeyword("dc:date")
    }
    data object Item {
        data object Tag: RdfKeyword("item")
        data object Title: RdfKeyword("title")
        data object Description: RdfKeyword("description")
        data object Link: RdfKeyword("link")
        data object DCDate: RdfKeyword("dc:date")
        data object DCCreator: RdfKeyword("dc:creator")
        data object SlashSection: RdfKeyword("slash:section")
    }
}
