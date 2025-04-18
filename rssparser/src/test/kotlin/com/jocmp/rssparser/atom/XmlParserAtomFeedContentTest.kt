package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage


class XmlParserAtomFeedContentTest : BaseParserTest(
    feedPath = "atom-feed-content.xml",
    channelImage = RssImage(url = "https://grapheneos.org/favicon.ico"),
    channelTitle = "GrapheneOS changelog",
    channelLink = "https://grapheneos.org/LICENSE.txt",
    channelLastBuildDate = "2025-04-11T00:00:00Z",
    articleTitle = "2025041100",
    articlePubDate = "2025-04-11T00:00:00Z",
    articleLink = "https://grapheneos.org/releases#2025041100",
    articleGuid = "https://grapheneos.org/releases#2025041100",
    articleContent = """<div xmlns="http://www.w3.org/1999/xhtml">
                <p>Tags:</p>
                <ul>
                    <li>
                        <a href="https://github.com/GrapheneOS/platform_manifest/releases/tag/2025041100">
                            2025041100
                        </a>
                        (Pixel 6, Pixel 6 Pro, Pixel 6a, Pixel 7, Pixel 7 Pro, Pixel 7a, Pixel
                        Tablet, Pixel Fold, Pixel 8, Pixel 8 Pro, Pixel 8a, Pixel 9, Pixel 9 Pro,
                        Pixel 9 Pro XL, Pixel 9 Pro Fold, emulator, generic, other targets)
                    </li>
                </ul>
            </div>""",
)
