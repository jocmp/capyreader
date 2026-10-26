package com.jocmp.mercury.extractors.custom.www.jnsa.org

import com.jocmp.mercury.extractors.extractor

val WwwJnsaOrgExtractor =
    extractor("www.jnsa.org") {
        title { selectors("#wgtitle h2") }

        excerpt { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#main_area")
            clean("#pankuzu", "#side")
        }
    }
