package com.jocmp.capy.accounts.reader

import com.jocmp.readerclient.Item
import com.jocmp.readerclient.Item.Link
import com.jocmp.readerclient.Item.Origin
import com.jocmp.readerclient.Item.Summary

object ItemFixtures {
    val item = Item(
        id = "tag:google.com,2005:reader/item/0000000000000010",
        published = 1723806013,
        title = "Rocket Report: ULA is losing engineers; SpaceX is launching every two days",
        canonical = listOf(Link("https://arstechnica.com/?p=2043638")),
        origin = Origin(
            streamId = "feed/2",
            title = "Ars Technica - All content",
            htmlUrl = "https://arstechnica.com",
        ),
        categories = listOf(
            "user/-/label/Tech"
        ),
        summary = Summary("Summary - Welcome to Edition 7.07 of the Rocket Report! SpaceX has not missed a beat since the Federal Aviation Administration gave the company a green light to resume Falcon 9 launches after a failure last month."),
        content = Item.Content("Content - Welcome to Edition 7.07 of the Rocket Report! SpaceX has not missed a beat since the Federal Aviation Administration gave the company a green light to resume Falcon 9 launches after a failure last month."),
    )
}
