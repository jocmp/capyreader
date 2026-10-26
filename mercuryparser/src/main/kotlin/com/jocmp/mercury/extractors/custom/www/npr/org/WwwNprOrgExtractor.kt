package com.jocmp.mercury.extractors.custom.www.npr.org

import com.jocmp.mercury.extractors.TransformResult
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

            // Brightspot's data-template replaces src during lazy-image conversion,
            // and the CDN answers its unexpanded tokens with a placeholder image.
            transform("img[src*=\"{width}\"], img[src*=\"{quality}\"], img[src*=\"{format}\"]") { node, _ ->
                node.elements.forEach { img ->
                    img.attr(
                        "src",
                        img.attr("src")
                            .replace("{width}", "1200")
                            .replace("{quality}", "85")
                            .replace("{format}", img.attr("data-format").ifBlank { "webp" }),
                    )
                }
                TransformResult.NoChange
            }

            clean("div.enlarge_measure")
        }
    }
