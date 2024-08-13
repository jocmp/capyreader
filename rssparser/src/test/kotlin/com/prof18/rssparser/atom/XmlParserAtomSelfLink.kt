package com.prof18.rssparser.atom

import com.prof18.rssparser.BaseXmlParserTest

class XmlParserAtomSelfLink : BaseXmlParserTest(
    feedPath = "atom-self-link-example.xml",
    channelTitle = "Simon Willison's Weblog",
    channelLink = "http://simonwillison.net/",
    channelLastBuildDate = "2023-07-29T21:23:21+00:00",
    articleTitle = "A Steering Council notice about PEP 703 (Making the Global Interpreter Lock Optional\n" +
            "            in CPython)",
    articlePubDate = "2023-07-29T21:23:21+00:00",
    articleLink = "http://simonwillison.net/2023/Jul/29/a-steering-council-notice-about-pep-703/#atom-everything",
    articleGuid = "http://simonwillison.net/2023/Jul/29/a-steering-council-notice-about-pep-703/#atom-everything",
    articleDescription = "<p><a\n" +
            "            href=\"https://discuss.python.org/t/a-steering-council-notice-about-pep-703-making-the-global-interpreter-lock-optional-in-cpython/30474\">A\n" +
            "            Steering Council notice about PEP 703 (Making the Global Interpreter Lock Optional in\n" +
            "            CPython)</a></p> <p>Huge news concerning the nogil research fork of\n" +
            "            Python: &quot; It’s clear that the overall sentiment is positive, both for the\n" +
            "            general idea and for PEP 703 specifically. The Steering Council is also largely positive\n" +
            "            on both. We intend to accept PEP 703, although we’re still working on the acceptance\n" +
            "            details.&quot;</p>\n" +
            "\n" +
            "            <p>Via <a href=\"https://twitter.com/charliermarsh/status/1685068890424967168\">@charliermarsh</a></p>"
)
