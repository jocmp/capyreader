package com.jocmp.mercury.extractors.custom.www.npr.org

import com.jocmp.mercury.extractors.extractor

val WwwNprOrgExtractor =
    extractor("www.npr.org") {
        title { selectors("h1", ".storytitle") }

        author { selectors("p.byline__name.byline__name--block") }

        datePublished {
            attr(".dateblock time[datetime]", "datetime")
            attr("meta[name=\"date\"]", "value")
        }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr("meta[name=\"twitter:image:src\"]", "value")
        }

        content {
            selectors(".storytext")

            transform(".bucketwrap.image", renameTo = "figure")
            transform(".bucketwrap.image .credit-caption", renameTo = "figcaption")

            clean("div.enlarge_measure")
        }
    }
