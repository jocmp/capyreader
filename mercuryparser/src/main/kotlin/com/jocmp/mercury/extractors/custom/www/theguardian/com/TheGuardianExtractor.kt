package com.jocmp.mercury.extractors.custom.www.theguardian.com

import com.jocmp.mercury.extractors.extractor

// Port note: parity test fails on dek. Selector matches upstream verbatim and
// returns the expected text, but cleanDek nulls it out (likely because excerpt
// extraction yields the same string, tripping the equality guard). Same class of
// failure as the pre-existing abcnews.go.com port — not specific to this extractor.
val TheGuardianExtractor =
    extractor("www.theguardian.com") {
        title { selectors("h1", ".content__headline") }

        author { selectors("address[data-link-name=\"byline\"]", "p.byline") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("div[data-gu-name=\"standfirst\"]", ".content__standfirst") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#maincontent", ".content__article-body")

            clean(".hide-on-mobile", ".inline-icon")
        }
    }
