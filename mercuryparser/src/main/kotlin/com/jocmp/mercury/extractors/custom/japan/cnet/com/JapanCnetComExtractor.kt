package com.jocmp.mercury.extractors.custom.japan.cnet.com

import com.jocmp.mercury.extractors.extractor

val JapanCnetComExtractor =
    extractor("japan.cnet.com") {
        title { selectors(".leaf-headline-ttl") }

        author { selectors(".writer") }

        datePublished {
            selectors(".date")
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日 HH時mm分"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content { selectors("div.article_body") }
    }
