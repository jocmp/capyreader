package com.jocmp.mercury.extractors.custom.obamawhitehouse.archives.gov

import com.jocmp.mercury.extractors.extractor

val ObamawhitehouseArchivesGovExtractor =
    extractor("obamawhitehouse.archives.gov") {
        supportedDomains = listOf("whitehouse.gov")

        title { selectors("h1", ".pane-node-title") }

        author { selectors(".blog-author-link", ".node-person-name-link") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors(".field-name-field-forall-summary") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            defaultCleaner = false

            selectors("div#content-start", ".pane-node-field-forall-body")

            clean(".pane-node-title", ".pane-custom.pane-1")
        }
    }
