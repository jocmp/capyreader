package com.jocmp.mercury.extractors.custom.clinicaltrials.gov

import com.jocmp.mercury.extractors.extractor

val ClinicaltrialsGovExtractor =
    extractor("clinicaltrials.gov") {
        title { selectors("h1.tr-solo_record") }

        author { selectors("div#sponsor.tr-info-text") }

        datePublished {
            // selectors: ['span.term[data-term="Last Update Posted"]'],
            selectors("div:has(> span.term[data-term=\"Last Update Posted\"])")
        }

        content {
            selectors("div#tab-body")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(".usa-alert> img")
        }
    }
