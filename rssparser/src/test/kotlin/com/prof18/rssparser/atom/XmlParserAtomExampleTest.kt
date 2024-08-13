package com.prof18.rssparser.atom

import com.prof18.rssparser.BaseXmlParserTest

class XmlParserAtomExampleTest: BaseXmlParserTest(
    feedPath = "atom-test-example.xml",
    channelTitle = "Example Feed",
    channelLink = "http://example.org/",
    channelDescription = "A subtitle.",
    channelLastBuildDate = "2003-12-13T18:30:02Z",
    articleGuid = "urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a",
    articleTitle = "Atom-Powered Robots Run Amok",
    articleAuthor = "John Doe",
    articleLink = "http://example.org/2003/12/13/atom03.html",
    articlePubDate = "2003-12-13T18:30:02Z",
    articleDescription = "Some text.",
    articleContent = "This is the entry content.",
)
