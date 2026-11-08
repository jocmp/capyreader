package com.jocmp.mallet.tex

import java.text.Normalizer

data class AccentCommands(val text: String?, val math: String?)

object UnicodeTables {
    val unicodeAccents: Map<String, AccentCommands> = linkedMapOf(
        "́" to AccentCommands(text = "\\'", math = "\\acute"),
        "̀" to AccentCommands(text = "\\`", math = "\\grave"),
        "̈" to AccentCommands(text = "\\\"", math = "\\ddot"),
        "̃" to AccentCommands(text = "\\~", math = "\\tilde"),
        "̄" to AccentCommands(text = "\\=", math = "\\bar"),
        "̆" to AccentCommands(text = "\\u", math = "\\breve"),
        "̌" to AccentCommands(text = "\\v", math = "\\check"),
        "̂" to AccentCommands(text = "\\^", math = "\\hat"),
        "̇" to AccentCommands(text = "\\.", math = "\\dot"),
        "̊" to AccentCommands(text = "\\r", math = "\\mathring"),
        "̋" to AccentCommands(text = "\\H", math = null),
        "̧" to AccentCommands(text = "\\c", math = null),
    )

    val unicodeSymbols: Map<String, String> = buildUnicodeSymbols()

    val unicodeSubRegex = Regex("^[₊₋₌₍₎₀₁₂₃₄₅₆₇₈₉ₐₑₕᵢⱼₖₗₘₙₒₚᵣₛₜᵤᵥₓᵦᵧᵨᵩᵪ]")

    val uSubsAndSups: Map<String, String> = linkedMapOf(
        "₊" to "+",
        "₋" to "-",
        "₌" to "=",
        "₍" to "(",
        "₎" to ")",
        "₀" to "0",
        "₁" to "1",
        "₂" to "2",
        "₃" to "3",
        "₄" to "4",
        "₅" to "5",
        "₆" to "6",
        "₇" to "7",
        "₈" to "8",
        "₉" to "9",
        "\u2090" to "a",
        "\u2091" to "e",
        "\u2095" to "h",
        "\u1D62" to "i",
        "\u2C7C" to "j",
        "\u2096" to "k",
        "\u2097" to "l",
        "\u2098" to "m",
        "\u2099" to "n",
        "\u2092" to "o",
        "\u209A" to "p",
        "\u1D63" to "r",
        "\u209B" to "s",
        "\u209C" to "t",
        "\u1D64" to "u",
        "\u1D65" to "v",
        "\u2093" to "x",
        "\u1D66" to "β",
        "\u1D67" to "γ",
        "\u1D68" to "ρ",
        "\u1D69" to "\u03d5",
        "\u1D6A" to "χ",
        "⁺" to "+",
        "⁻" to "-",
        "⁼" to "=",
        "⁽" to "(",
        "⁾" to ")",
        "⁰" to "0",
        "¹" to "1",
        "²" to "2",
        "³" to "3",
        "⁴" to "4",
        "⁵" to "5",
        "⁶" to "6",
        "⁷" to "7",
        "⁸" to "8",
        "⁹" to "9",
        "\u1D2C" to "A",
        "\u1D2E" to "B",
        "\u1D30" to "D",
        "\u1D31" to "E",
        "\u1D33" to "G",
        "\u1D34" to "H",
        "\u1D35" to "I",
        "\u1D36" to "J",
        "\u1D37" to "K",
        "\u1D38" to "L",
        "\u1D39" to "M",
        "\u1D3A" to "N",
        "\u1D3C" to "O",
        "\u1D3E" to "P",
        "\u1D3F" to "R",
        "\u1D40" to "T",
        "\u1D41" to "U",
        "\u2C7D" to "V",
        "\u1D42" to "W",
        "\u1D43" to "a",
        "\u1D47" to "b",
        "\u1D9C" to "c",
        "\u1D48" to "d",
        "\u1D49" to "e",
        "\u1DA0" to "f",
        "\u1D4D" to "g",
        "\u02B0" to "h",
        "\u2071" to "i",
        "\u02B2" to "j",
        "\u1D4F" to "k",
        "\u02E1" to "l",
        "\u1D50" to "m",
        "\u207F" to "n",
        "\u1D52" to "o",
        "\u1D56" to "p",
        "\u02B3" to "r",
        "\u02E2" to "s",
        "\u1D57" to "t",
        "\u1D58" to "u",
        "\u1D5B" to "v",
        "\u02B7" to "w",
        "\u02E3" to "x",
        "\u02B8" to "y",
        "\u1DBB" to "z",
        "\u1D5D" to "β",
        "\u1D5E" to "γ",
        "\u1D5F" to "δ",
        "\u1D60" to "\u03d5",
        "\u1D61" to "χ",
        "\u1DBF" to "θ",
    )

    private fun buildUnicodeSymbols(): Map<String, String> {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "αβγδεϵζηθϑικλμνξοπϖρϱςστυφϕχψωΓΔΘΛΞΠΣΥΦΨΩ"
        val accents = unicodeAccents.keys
        val result = LinkedHashMap<String, String>()

        letters.forEach { letter ->
            accents.forEach { accent ->
                val combined = "$letter$accent"
                val normalized = Normalizer.normalize(combined, Normalizer.Form.NFC)
                if (normalized.length == 1) {
                    result[normalized] = combined
                }
                accents.filter { it != accent }.forEach { accent2 ->
                    val combined2 = combined + accent2
                    val normalized2 = Normalizer.normalize(combined2, Normalizer.Form.NFC)
                    if (normalized2.length == 1) {
                        result[normalized2] = combined2
                    }
                }
            }
        }

        return result
    }
}

private class Script(val name: String, val blocks: List<IntRange>)

private val scriptData = listOf(
    Script("latin", listOf(0x0100..0x024f, 0x0300..0x036f)),
    Script("cyrillic", listOf(0x0400..0x04ff)),
    Script("armenian", listOf(0x0530..0x058F)),
    Script("brahmic", listOf(0x0900..0x109F)),
    Script("georgian", listOf(0x10A0..0x10ff)),
    Script("cjk", listOf(0x3000..0x30FF, 0x4E00..0x9FAF, 0xFF00..0xFF60)),
    Script("hangul", listOf(0xAC00..0xD7AF)),
)

private val allBlocks = scriptData.flatMap { it.blocks }

fun scriptFromCodepoint(codepoint: Int): String? =
    scriptData.firstOrNull { script -> script.blocks.any { codepoint in it } }?.name

fun supportedCodepoint(codepoint: Int): Boolean = allBlocks.any { codepoint in it }
