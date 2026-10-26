package com.jocmp.mercury.extractors.custom.www.elecom.co.jp

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwElecomCoJpExtractor =
    extractor("www.elecom.co.jp") {
        title { selectors("title") }

        datePublished {
            timezone = "Asia/Tokyo"
            format = "YYYY.M.D"
            selectors("p.section-last")
        }

        content {
            selectors("td.TableMain2")

            defaultCleaner = false

            transform("table") { node, _ ->
                node.attr("width", "auto")
                TransformResult.NoChange
            }
        }
    }
