package com.jocmp.mercury.extractors.custom.www.itmedia.co.jp

import com.jocmp.mercury.extractors.extractor

val WwwItmediaCoJpExtractor =
    extractor("www.itmedia.co.jp") {
        supportedDomains =
            listOf(
                "www.atmarkit.co.jp",
                "techtarget.itmedia.co.jp",
                "nlab.itmedia.co.jp",
            )

        title { selectors("#cmsTitle h1") }

        author { selectors("#byline") }

        datePublished { attr("meta[name=\"article:modified_time\"]", "value") }

        dek { selectors("#cmsAbstract h2") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#cmsBody")
            defaultCleaner = false
            clean("#snsSharebox")
        }
    }
