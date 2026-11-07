package com.jocmp.mallet.math

import com.jocmp.mallet.math.AtomType.BINARY_OPERATOR
import com.jocmp.mallet.math.AtomType.CLOSE
import com.jocmp.mallet.math.AtomType.OPEN
import com.jocmp.mallet.math.AtomType.ORDINARY
import com.jocmp.mallet.math.AtomType.PUNCTUATION
import com.jocmp.mallet.math.AtomType.RELATION
import com.jocmp.mallet.math.AtomType.VARIABLE

/**
 * TeX command tables, scoped to what KaTeX renders.
 */
internal object MathSymbols {
    /**
     * A fresh atom for [name], or null if it is not a symbol, operator, space or style command.
     */
    fun atom(name: String): MathAtom? {
        val canonical = aliases[name] ?: name

        symbols[canonical]?.let { (type, nucleus) ->
            return MathAtom(type, nucleus)
        }
        operators[canonical]?.let { (nucleus, limits) ->
            return LargeOperator(nucleus, limits)
        }
        spaces[canonical]?.let { return MathSpace(it) }
        styles[canonical]?.let { return MathStyle(it) }

        return null
    }

    fun accent(name: String): String? = accents[name]

    fun delimiter(name: String): String? = delimiters[name]

    fun fontStyle(name: String): FontStyle? = fontStyles[name]

    fun isTextCommand(name: String): Boolean = name in textCommands

    /**
     * The symbol for a non-ASCII character typed directly in the source, such as `×` or `≤`.
     */
    fun atomForCodepoint(char: String): MathAtom {
        val type = typeByNucleus[char] ?: ORDINARY
        return MathAtom(type, char)
    }

    /**
     * The negated form of a relation for `\not`, or null if there is no precomposed character.
     */
    fun negated(nucleus: String): String? = negations[nucleus]

    /**
     * ARGB for a CSS color name or `#RRGGBB` / `RRGGBB` / `#RGB` hex value.
     */
    fun color(value: String): Int? {
        namedColors[value.lowercase()]?.let { return it }

        val hex = value.removePrefix("#")
        val expanded =
            when (hex.length) {
                3 -> hex.map { "$it$it" }.joinToString("")
                6 -> hex
                else -> return null
            }

        return expanded.toLongOrNull(16)?.let { (0xFF000000 or it).toInt() }
    }

    private val symbols: Map<String, Pair<AtomType, String>> =
        buildMap {
            fun put(type: AtomType, vararg entries: Pair<String, String>) {
                entries.forEach { (name, nucleus) -> put(name, type to nucleus) }
            }

            put(
                VARIABLE,
                "alpha" to "α",
                "beta" to "β",
                "gamma" to "γ",
                "delta" to "δ",
                "varepsilon" to "ε",
                "zeta" to "ζ",
                "eta" to "η",
                "theta" to "θ",
                "iota" to "ι",
                "kappa" to "κ",
                "lambda" to "λ",
                "mu" to "μ",
                "nu" to "ν",
                "xi" to "ξ",
                "omicron" to "ο",
                "pi" to "π",
                "rho" to "ρ",
                "varsigma" to "ς",
                "sigma" to "σ",
                "tau" to "τ",
                "upsilon" to "υ",
                "varphi" to "φ",
                "chi" to "χ",
                "psi" to "ψ",
                "omega" to "ω",
                "vartheta" to "ϑ",
                "phi" to "ϕ",
                "varpi" to "ϖ",
                "varkappa" to "ϰ",
                "varrho" to "ϱ",
                "epsilon" to "ϵ",
                "Gamma" to "Γ",
                "Delta" to "Δ",
                "Theta" to "Θ",
                "Lambda" to "Λ",
                "Xi" to "Ξ",
                "Pi" to "Π",
                "Sigma" to "Σ",
                "Upsilon" to "Υ",
                "Phi" to "Φ",
                "Psi" to "Ψ",
                "Omega" to "Ω",
            )

            put(
                OPEN,
                "lceil" to "⌈",
                "lfloor" to "⌊",
                "langle" to "⟨",
                "lgroup" to "⟮",
                "lvert" to "|",
                "lVert" to "‖",
                "lbrack" to "[",
                "{" to "{",
            )

            put(
                CLOSE,
                "rceil" to "⌉",
                "rfloor" to "⌋",
                "rangle" to "⟩",
                "rgroup" to "⟯",
                "rvert" to "|",
                "rVert" to "‖",
                "rbrack" to "]",
                "}" to "}",
            )

            put(
                RELATION,
                "leftarrow" to "←",
                "uparrow" to "↑",
                "rightarrow" to "→",
                "downarrow" to "↓",
                "leftrightarrow" to "↔",
                "updownarrow" to "↕",
                "nwarrow" to "↖",
                "nearrow" to "↗",
                "searrow" to "↘",
                "swarrow" to "↙",
                "mapsto" to "↦",
                "hookleftarrow" to "↩",
                "hookrightarrow" to "↪",
                "leftharpoonup" to "↼",
                "rightharpoonup" to "⇀",
                "rightleftharpoons" to "⇌",
                "leftrightarrows" to "⇆",
                "rightleftarrows" to "⇄",
                "Leftarrow" to "⇐",
                "Uparrow" to "⇑",
                "Rightarrow" to "⇒",
                "Downarrow" to "⇓",
                "Leftrightarrow" to "⇔",
                "Updownarrow" to "⇕",
                "longleftarrow" to "⟵",
                "longrightarrow" to "⟶",
                "longleftrightarrow" to "⟷",
                "Longleftarrow" to "⟸",
                "Longrightarrow" to "⟹",
                "Longleftrightarrow" to "⟺",
                "longmapsto" to "⟼",
                "lt" to "<",
                "gt" to ">",
                "leq" to "≤",
                "geq" to "≥",
                "leqslant" to "⩽",
                "geqslant" to "⩾",
                "neq" to "≠",
                "nleq" to "≰",
                "ngeq" to "≱",
                "nless" to "≮",
                "ngtr" to "≯",
                "lesssim" to "≲",
                "gtrsim" to "≳",
                "in" to "∈",
                "notin" to "∉",
                "ni" to "∋",
                "propto" to "∝",
                "mid" to "∣",
                "nmid" to "∤",
                "parallel" to "∥",
                "nparallel" to "∦",
                "sim" to "∼",
                "simeq" to "≃",
                "cong" to "≅",
                "approx" to "≈",
                "approxeq" to "≊",
                "asymp" to "≍",
                "doteq" to "≐",
                "triangleq" to "≜",
                "equiv" to "≡",
                "nequiv" to "≢",
                "ll" to "≪",
                "gg" to "≫",
                "lll" to "⋘",
                "ggg" to "⋙",
                "prec" to "≺",
                "succ" to "≻",
                "preceq" to "⪯",
                "succeq" to "⪰",
                "subset" to "⊂",
                "supset" to "⊃",
                "subseteq" to "⊆",
                "supseteq" to "⊇",
                "subsetneq" to "⊊",
                "supsetneq" to "⊋",
                "nsubseteq" to "⊈",
                "nsupseteq" to "⊉",
                "sqsubset" to "⊏",
                "sqsupset" to "⊐",
                "sqsubseteq" to "⊑",
                "sqsupseteq" to "⊒",
                "vdash" to "⊢",
                "dashv" to "⊣",
                "models" to "⊧",
                "vDash" to "⊨",
                "perp" to "⟂",
                "coloneqq" to "≔",
                "eqqcolon" to "≕",
                "bowtie" to "⋈",
            )

            put(
                BINARY_OPERATOR,
                "times" to "×",
                "div" to "÷",
                "pm" to "±",
                "mp" to "∓",
                "dagger" to "†",
                "ddagger" to "‡",
                "setminus" to "∖",
                "smallsetminus" to "∖",
                "ast" to "∗",
                "circ" to "∘",
                "bullet" to "∙",
                "wedge" to "∧",
                "vee" to "∨",
                "cap" to "∩",
                "cup" to "∪",
                "wr" to "≀",
                "uplus" to "⊎",
                "sqcap" to "⊓",
                "sqcup" to "⊔",
                "oplus" to "⊕",
                "ominus" to "⊖",
                "otimes" to "⊗",
                "oslash" to "⊘",
                "odot" to "⊙",
                "star" to "⋆",
                "cdot" to "⋅",
                "amalg" to "⨿",
                "diamond" to "⋄",
                "bigtriangleup" to "△",
                "bigtriangledown" to "▽",
                "triangleleft" to "◁",
                "triangleright" to "▷",
                "ltimes" to "⋉",
                "rtimes" to "⋊",
                "bmod" to "mod",
            )

            put(
                PUNCTUATION,
                "colon" to ":",
                "cdotp" to "·",
                "ldotp" to ".",
            )

            put(
                ORDINARY,
                "$" to "$",
                "&" to "&",
                "#" to "#",
                "%" to "%",
                "_" to "_",
                " " to " ",
                "backslash" to "\\",
                "degree" to "°",
                "neg" to "¬",
                "angstrom" to "Å",
                "|" to "‖",
                "vert" to "|",
                "ldots" to "…",
                "dots" to "…",
                "dotsc" to "…",
                "dotso" to "…",
                "prime" to "′",
                "backprime" to "‵",
                "hbar" to "ℏ",
                "hslash" to "ℏ",
                "Im" to "ℑ",
                "ell" to "ℓ",
                "wp" to "℘",
                "Re" to "ℜ",
                "mho" to "℧",
                "aleph" to "ℵ",
                "beth" to "ℶ",
                "forall" to "∀",
                "complement" to "∁",
                "exists" to "∃",
                "nexists" to "∄",
                "emptyset" to "∅",
                "varnothing" to "∅",
                "nabla" to "∇",
                "infty" to "∞",
                "angle" to "∠",
                "measuredangle" to "∡",
                "top" to "⊤",
                "bot" to "⊥",
                "vdots" to "⋮",
                "cdots" to "⋯",
                "dotsb" to "⋯",
                "dotsm" to "⋯",
                "dotsi" to "⋯",
                "ddots" to "⋱",
                "triangle" to "△",
                "square" to "□",
                "Box" to "□",
                "blacksquare" to "■",
                "lozenge" to "◊",
                "checkmark" to "✓",
                "clubsuit" to "♣",
                "diamondsuit" to "♢",
                "heartsuit" to "♡",
                "spadesuit" to "♠",
                "flat" to "♭",
                "natural" to "♮",
                "sharp" to "♯",
                "imath" to "𝚤",
                "jmath" to "𝚥",
                "partial" to "𝜕",
                "S" to "§",
                "P" to "¶",
                "copyright" to "©",
                "pounds" to "£",
                "yen" to "¥",
                "euro" to "€",
            )
        }

    private val operators: Map<String, Pair<String, Boolean>> =
        mapOf(
            "log" to ("log" to false),
            "lg" to ("lg" to false),
            "ln" to ("ln" to false),
            "sin" to ("sin" to false),
            "arcsin" to ("arcsin" to false),
            "sinh" to ("sinh" to false),
            "cos" to ("cos" to false),
            "arccos" to ("arccos" to false),
            "cosh" to ("cosh" to false),
            "tan" to ("tan" to false),
            "arctan" to ("arctan" to false),
            "tanh" to ("tanh" to false),
            "cot" to ("cot" to false),
            "coth" to ("coth" to false),
            "sec" to ("sec" to false),
            "csc" to ("csc" to false),
            "arg" to ("arg" to false),
            "ker" to ("ker" to false),
            "dim" to ("dim" to false),
            "hom" to ("hom" to false),
            "exp" to ("exp" to false),
            "deg" to ("deg" to false),
            "lim" to ("lim" to true),
            "limsup" to ("lim sup" to true),
            "liminf" to ("lim inf" to true),
            "max" to ("max" to true),
            "min" to ("min" to true),
            "sup" to ("sup" to true),
            "inf" to ("inf" to true),
            "det" to ("det" to true),
            "Pr" to ("Pr" to true),
            "gcd" to ("gcd" to true),
            "argmax" to ("arg max" to true),
            "argmin" to ("arg min" to true),
            "prod" to ("∏" to true),
            "coprod" to ("∐" to true),
            "sum" to ("∑" to true),
            "int" to ("∫" to false),
            "iint" to ("∬" to false),
            "iiint" to ("∭" to false),
            "oint" to ("∮" to false),
            "bigwedge" to ("⋀" to true),
            "bigvee" to ("⋁" to true),
            "bigcap" to ("⋂" to true),
            "bigcup" to ("⋃" to true),
            "bigodot" to ("⨀" to true),
            "bigoplus" to ("⨁" to true),
            "bigotimes" to ("⨂" to true),
            "biguplus" to ("⨄" to true),
            "bigsqcup" to ("⨆" to true),
        )

    // In mu. \  and ~ are interword spaces; KaTeX uses 0.25em for \ (4.5mu) which rounds to 4.
    private val spaces: Map<String, Float> =
        mapOf(
            "," to 3f,
            "thinspace" to 3f,
            ":" to 4f,
            ">" to 4f,
            "medspace" to 4f,
            ";" to 5f,
            "thickspace" to 5f,
            "!" to -3f,
            "negthinspace" to -3f,
            "negmedspace" to -4f,
            "negthickspace" to -5f,
            "enspace" to 9f,
            "quad" to 18f,
            "qquad" to 36f,
        )

    private val styles: Map<String, LineStyle> =
        mapOf(
            "displaystyle" to LineStyle.DISPLAY,
            "textstyle" to LineStyle.TEXT,
            "scriptstyle" to LineStyle.SCRIPT,
            "scriptscriptstyle" to LineStyle.SCRIPT_SCRIPT,
        )

    private val aliases: Map<String, String> =
        mapOf(
            "lnot" to "neg",
            "land" to "wedge",
            "lor" to "vee",
            "ne" to "neq",
            "le" to "leq",
            "ge" to "geq",
            "lbrace" to "{",
            "rbrace" to "}",
            "Vert" to "|",
            "gets" to "leftarrow",
            "to" to "rightarrow",
            "implies" to "Longrightarrow",
            "impliedby" to "Longleftarrow",
            "iff" to "Longleftrightarrow",
            "AA" to "angstrom",
            "textbackslash" to "backslash",
            "owns" to "ni",
            "mathellipsis" to "ldots",
            "textellipsis" to "ldots",
            "intop" to "int",
            "smallint" to "int",
        )

    private val accents: Map<String, String> =
        mapOf(
            "grave" to "̀",
            "acute" to "́",
            "hat" to "̂",
            "widehat" to "̂",
            "tilde" to "̃",
            "widetilde" to "̃",
            "bar" to "̄",
            "breve" to "̆",
            "dot" to "̇",
            "ddot" to "̈",
            "mathring" to "̊",
            "check" to "̌",
            "vec" to "⃗",
            "overrightarrow" to "⃗",
            "overleftarrow" to "⃖",
        )

    private val delimiters: Map<String, String> =
        mapOf(
            "." to "",
            "(" to "(",
            ")" to ")",
            "[" to "[",
            "]" to "]",
            "<" to "⟨",
            ">" to "⟩",
            "/" to "/",
            "\\" to "\\",
            "|" to "|",
            "lgroup" to "⟮",
            "rgroup" to "⟯",
            "||" to "‖",
            "Vert" to "‖",
            "vert" to "|",
            "lvert" to "|",
            "rvert" to "|",
            "lVert" to "‖",
            "rVert" to "‖",
            "uparrow" to "↑",
            "downarrow" to "↓",
            "updownarrow" to "↕",
            "Uparrow" to "⇑",
            "Downarrow" to "⇓",
            "Updownarrow" to "⇕",
            "backslash" to "\\",
            "rangle" to "⟩",
            "langle" to "⟨",
            "rbrace" to "}",
            "}" to "}",
            "{" to "{",
            "lbrace" to "{",
            "lceil" to "⌈",
            "rceil" to "⌉",
            "lfloor" to "⌊",
            "rfloor" to "⌋",
        )

    private val fontStyles: Map<String, FontStyle> =
        mapOf(
            "mathnormal" to FontStyle.DEFAULT,
            "mathrm" to FontStyle.ROMAN,
            "textrm" to FontStyle.ROMAN,
            "textnormal" to FontStyle.ROMAN,
            "textup" to FontStyle.ROMAN,
            "rm" to FontStyle.ROMAN,
            "text" to FontStyle.ROMAN,
            "mbox" to FontStyle.ROMAN,
            "hbox" to FontStyle.ROMAN,
            "mathbf" to FontStyle.BOLD,
            "bf" to FontStyle.BOLD,
            "textbf" to FontStyle.BOLD,
            "mathcal" to FontStyle.CALIGRAPHIC,
            "mathscr" to FontStyle.CALIGRAPHIC,
            "cal" to FontStyle.CALIGRAPHIC,
            "mathtt" to FontStyle.TYPEWRITER,
            "texttt" to FontStyle.TYPEWRITER,
            "mathit" to FontStyle.ITALIC,
            "textit" to FontStyle.ITALIC,
            "emph" to FontStyle.ITALIC,
            "it" to FontStyle.ITALIC,
            "mathsf" to FontStyle.SANS_SERIF,
            "textsf" to FontStyle.SANS_SERIF,
            "mathfrak" to FontStyle.FRAKTUR,
            "frak" to FontStyle.FRAKTUR,
            "mathbb" to FontStyle.BLACKBOARD,
            "Bbb" to FontStyle.BLACKBOARD,
            "mathbfit" to FontStyle.BOLD_ITALIC,
            "bm" to FontStyle.BOLD_ITALIC,
            "boldsymbol" to FontStyle.BOLD_ITALIC,
        )

    private val textCommands =
        setOf("text", "textrm", "textnormal", "textup", "textbf", "textit", "textsf", "texttt", "emph", "mbox", "hbox")

    private val negations: Map<String, String> =
        mapOf(
            "=" to "≠",
            "<" to "≮",
            ">" to "≯",
            "≤" to "≰",
            "≥" to "≱",
            "∈" to "∉",
            "∋" to "∌",
            "≡" to "≢",
            "∼" to "≁",
            "≃" to "≄",
            "≅" to "≇",
            "≈" to "≉",
            "⊂" to "⊄",
            "⊃" to "⊅",
            "⊆" to "⊈",
            "⊇" to "⊉",
            "∣" to "∤",
            "∥" to "∦",
            "≺" to "⊀",
            "≻" to "⊁",
            "⊢" to "⊬",
            "∃" to "∄",
            "→" to "↛",
            "←" to "↚",
            "⇒" to "⇏",
            "⇔" to "⇎",
        )

    private val typeByNucleus: Map<String, AtomType> =
        buildMap {
            symbols.values.forEach { (type, nucleus) -> putIfAbsent(nucleus, type) }
            put("−", BINARY_OPERATOR)
            put("∶", RELATION)
        }

    private val namedColors: Map<String, Int> =
        mapOf(
            "black" to 0xFF000000.toInt(),
            "white" to 0xFFFFFFFF.toInt(),
            "red" to 0xFFFF0000.toInt(),
            "green" to 0xFF008000.toInt(),
            "blue" to 0xFF0000FF.toInt(),
            "cyan" to 0xFF00FFFF.toInt(),
            "magenta" to 0xFFFF00FF.toInt(),
            "yellow" to 0xFFFFFF00.toInt(),
            "orange" to 0xFFFFA500.toInt(),
            "purple" to 0xFF800080.toInt(),
            "violet" to 0xFFEE82EE.toInt(),
            "brown" to 0xFFA52A2A.toInt(),
            "gray" to 0xFF808080.toInt(),
            "grey" to 0xFF808080.toInt(),
            "darkgray" to 0xFFA9A9A9.toInt(),
            "lightgray" to 0xFFD3D3D3.toInt(),
            "pink" to 0xFFFFC0CB.toInt(),
            "teal" to 0xFF008080.toInt(),
            "olive" to 0xFF808000.toInt(),
            "lime" to 0xFF00FF00.toInt(),
            "navy" to 0xFF000080.toInt(),
            "maroon" to 0xFF800000.toInt(),
            "gold" to 0xFFFFD700.toInt(),
        )
}
