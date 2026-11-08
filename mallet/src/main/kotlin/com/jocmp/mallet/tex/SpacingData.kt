package com.jocmp.mallet.tex

private val thinspace = Measurement(3.0, "mu")
private val mediumspace = Measurement(4.0, "mu")
private val thickspace = Measurement(5.0, "mu")

/** Inter-atom spacing for display and text styles, keyed by left then right atom class. */
val spacings: Map<String, Map<String, Measurement>> =
    mapOf(
        "mord" to mapOf("mop" to thinspace, "mbin" to mediumspace, "mrel" to thickspace, "minner" to thinspace),
        "mop" to mapOf("mord" to thinspace, "mop" to thinspace, "mrel" to thickspace, "minner" to thinspace),
        "mbin" to mapOf("mord" to mediumspace, "mop" to mediumspace, "mopen" to mediumspace, "minner" to mediumspace),
        "mrel" to mapOf("mord" to thickspace, "mop" to thickspace, "mopen" to thickspace, "minner" to thickspace),
        "mopen" to emptyMap(),
        "mclose" to mapOf("mop" to thinspace, "mbin" to mediumspace, "mrel" to thickspace, "minner" to thinspace),
        "mpunct" to
            mapOf(
                "mord" to thinspace,
                "mop" to thinspace,
                "mrel" to thickspace,
                "mopen" to thinspace,
                "mclose" to thinspace,
                "mpunct" to thinspace,
                "minner" to thinspace,
            ),
        "minner" to
            mapOf(
                "mord" to thinspace,
                "mop" to thinspace,
                "mbin" to mediumspace,
                "mrel" to thickspace,
                "mopen" to thinspace,
                "mpunct" to thinspace,
                "minner" to thinspace,
            ),
    )

/** Inter-atom spacing for script and scriptscript styles. */
val tightSpacings: Map<String, Map<String, Measurement>> =
    mapOf(
        "mord" to mapOf("mop" to thinspace),
        "mop" to mapOf("mord" to thinspace, "mop" to thinspace),
        "mbin" to emptyMap(),
        "mrel" to emptyMap(),
        "mopen" to emptyMap(),
        "mclose" to mapOf("mop" to thinspace),
        "mpunct" to emptyMap(),
        "minner" to mapOf("mop" to thinspace),
    )
