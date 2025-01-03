package com.jocmp.capy.articles

enum class TextSize(val px: Int) {
    XX_SMALL(px = 13),
    X_SMALL(px = 14),
    MEDIUM(px = 16),
    LARGE(px = 19),
    X_LARGE(px = 22),
    XX_LARGE(px = 25),
    XXX_LARGE(px = 32);

    companion object {
        val default = MEDIUM
    }
}
