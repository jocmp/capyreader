package com.jocmp.mallet.tex

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Formulas in the shape KaTeX-rendered blogs publish. Every one must lay out, so the reader only
 * falls back to TeX source for input KaTeX itself rejects.
 */
class TexCoverageTest {
    @Test
    fun corpusLaysOut() {
        val failures =
            corpus.mapNotNull { tex ->
                Tex.render(tex, displayMode = true).exceptionOrNull()?.let { "$tex -> ${it.stackTraceToString().lines().take(4).joinToString(" | ")}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun inlineCorpusLaysOut() {
        val failures =
            corpus.filterNot { tex -> displayOnly.any { tex.contains(it) } }.mapNotNull { tex ->
                Tex.render(tex, displayMode = false).exceptionOrNull()?.let { "$tex -> ${it.message}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun everySymbolCommandLaysOut() {
        val failures =
            symbolCommands.mapNotNull { name ->
                Tex.render("\\$name", displayMode = false).exceptionOrNull()?.let { "\\$name -> ${it.message}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun displayMathIsTallerThanInline() {
        val tex = "\\sum_{i=1}^n i = \\frac{n(n+1)}{2}"
        val display = Tex.render(tex, displayMode = true).getOrThrow().box
        val inline = Tex.render(tex, displayMode = false).getOrThrow().box

        assertTrue(display.height + display.depth > inline.height + inline.depth)
    }

    @Test
    fun symbolRadicandClearsTheSurd() {
        val items = Tex.render("\\frac{1+\\sqrt5}{2}", displayMode = false).getOrThrow().box.items
        val surd = items.filterIsInstance<TexItem.Svg>().single()
        val five = items.filterIsInstance<TexItem.Glyph>().single { it.text == "5" }

        assertTrue(five.x > surd.x + 0.3, "radicand at ${five.x}, surd at ${surd.x}")
    }

    @Test
    fun doubleIntegralAdvancesByItsGlyphWidth() {
        val items = Tex.render("\\iint_S (x)", displayMode = true).getOrThrow().box.items.filterIsInstance<TexItem.Glyph>()
        val integral = items.single { it.text == "\u222C" }
        val subscript = items.single { it.text == "S" }
        val paren = items.single { it.text == "(" }

        assertTrue(subscript.x - integral.x > 1.0, "subscript at ${subscript.x}, integral at ${integral.x}")
        assertTrue(paren.x > subscript.x + 0.4, "paren at ${paren.x}, subscript at ${subscript.x}")
    }

    @Test
    fun sizesAreCappedForUntrustedInput() {
        val box = Tex.render("\\rule{1500em}{200em}", displayMode = true).getOrThrow().box

        assertTrue(box.width <= 100.0 + 1e-6)
        assertTrue(box.height <= 100.0 + 1e-6)
    }

    @Test
    fun rejectsWhatKatexRejects() {
        assertTrue(Tex.render("\\frac{1}", displayMode = false).isFailure)
        assertTrue(Tex.render("\\notacommand", displayMode = false).isFailure)
        assertTrue(Tex.render("x \\tag{1}", displayMode = false).isFailure)
        assertTrue(Tex.render("\\begin{align} a \\end{align}", displayMode = false).isFailure)
        assertTrue(Tex.render("\\newcommand{\\R}{x}", displayMode = false).isFailure)
    }

    /** Writes PNGs of the corpus to build/tex-png when run with TEX_PNG=1. */
    @Test
    fun writePngs() {
        if (System.getenv("TEX_PNG") != "1") {
            return
        }
        val dir = File("build/tex-png")
        corpus.forEachIndexed { i, tex ->
            TexPng.write(tex, File(dir, "%02d.png".format(i)))
        }
    }

    companion object {
        private val displayOnly = listOf("\\tag", "{align}", "{CD}")

        val corpus =
            listOf(
                "F_0 = 0, F_1 = 1, F_{n+1} = F_n + F_{n-1}",
                "\\begin{pmatrix} F_{n+1} \\\\ F_n \\end{pmatrix} = \\begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\end{pmatrix} \\begin{pmatrix} F_n \\\\ F_{n-1} \\end{pmatrix}",
                "F_n = \\frac{1}{\\sqrt{5}} \\left( \\left( \\frac{1 + \\sqrt{5}}{2} \\right)^n - \\left( \\frac{1 - \\sqrt{5}}{2} \\right)^n \\right)",
                "\\varphi = \\frac{1+\\sqrt5}{2} \\approx 1.618",
                "\\int_0^\\infty e^{-x^2} \\, dx = \\frac{\\sqrt{\\pi}}{2}",
                "\\lim_{x \\to 0} \\frac{\\sin x}{x} = 1",
                "f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}",
                "\\frac{\\partial^2 u}{\\partial t^2} = c^2 \\nabla^2 u",
                "\\oint_C \\mathbf{F} \\cdot d\\mathbf{r} = \\iint_S (\\nabla \\times \\mathbf{F}) \\cdot d\\mathbf{S}",
                "\\sum_{k=0}^{\\infty} \\frac{x^k}{k!} = e^x",
                "A = U \\Sigma V^\\top",
                "\\det \\begin{vmatrix} a & b \\\\ c & d \\end{vmatrix} = ad - bc",
                "\\mathbb{E}[X] = \\sum_{x} x \\, \\Pr(X = x)",
                "P(A \\mid B) = \\frac{P(B \\mid A) P(A)}{P(B)}",
                "\\mathcal{N}(\\mu, \\sigma^2)",
                "\\| x \\|_2 = \\sqrt{\\sum_i x_i^2}",
                "\\langle u, v \\rangle = \\sum_{i=1}^n u_i \\bar{v}_i",
                "\\hat{y} = \\operatorname*{arg\\,max}_{y} p(y \\mid x)",
                "\\mathcal{L}(\\theta) = -\\frac{1}{N} \\sum_{i=1}^{N} \\log p_\\theta(y_i \\mid x_i)",
                "\\text{softmax}(z)_i = \\frac{e^{z_i}}{\\sum_j e^{z_j}}",
                "\\theta_{t+1} = \\theta_t - \\eta \\nabla_\\theta J(\\theta_t)",
                "|x| = \\begin{cases} x & \\text{if } x \\geq 0 \\\\ -x & \\text{otherwise} \\end{cases}",
                "\\begin{aligned} (a+b)^2 &= a^2 + 2ab + b^2 \\\\ &\\geq 4ab \\end{aligned}",
                "\\begin{bmatrix} 1 & 0 \\\\ 0 & 1 \\end{bmatrix}",
                "\\begin{array}{c|c} a & b \\\\ \\hline c & d \\end{array}",
                "\\begin{gathered} a = b \\\\ c = d \\end{gathered}",
                "a \\equiv b \\pmod{n}",
                "\\forall \\epsilon > 0 \\; \\exists \\delta > 0",
                "\\neg (p \\land q) \\iff \\neg p \\lor \\neg q",
                "\\gcd(a, b) = \\gcd(b, a \\bmod b)",
                "\\binom{n}{k} = \\frac{n!}{k!(n-k)!}",
                "\\lfloor x \\rfloor \\leq x < \\lfloor x \\rfloor + 1",
                "x \\in \\mathbb{R}^n, \\quad A \\subseteq B",
                "\\boxed{E = mc^2}",
                "\\boldsymbol{\\Sigma}^k = \\underbrace{\\left( \\mathbf{Q} \\boldsymbol{\\Lambda} \\mathbf{Q}^{\\top} \\right) \\dots \\left( \\mathbf{Q} \\boldsymbol{\\Lambda} \\mathbf{Q}^{\\top} \\right)}_{\\text{\$k\$ times}} = \\mathbf{Q} \\boldsymbol{\\Lambda}^k \\mathbf{Q}^{\\top}. \\tag{5}",
                "\\overbrace{1 + 1 + \\cdots + 1}^{n}",
                "\\overset{\\text{def}}{=}",
                "a \\xrightarrow{f} b \\xleftarrow[\\text{under}]{\\text{over}} c",
                "\\bigl( \\Bigl[ \\biggl\\{ x \\biggr\\} \\Bigr] \\bigr)",
                "\\left\\lVert \\frac{a}{b} \\right\\rVert",
                "\\left( \\frac{a}{b} \\middle| \\frac{c}{d} \\right)",
                "\\vec{v} = \\dot{x} \\hat{i} + \\ddot{y} \\tilde{j} + \\widehat{abc}",
                "\\overline{z} = \\underline{w}",
                "\\color{blue} x^2 + \\textcolor{red}{y}",
                "\\displaystyle \\sum_{n=1}^\\infty \\frac{1}{n^2} = \\frac{\\pi^2}{6} \\tag{1}",
                "\\sqrt[3]{x} \\cdot \\sqrt[n]{y} \\cdot \\sqrt{\\frac{a}{b}}",
                "x \\not\\in A, \\; y \\neq z",
                "\\dfrac{1}{2} + \\tfrac{3}{4}",
                "\\cdots \\ldots \\vdots \\ddots",
                "100\\% \\; \\$5 \\; \\#1",
                "\\newcommand{\\RR}{\\mathbb{R}} f: \\RR \\to \\RR",
                "\\def\\abs#1{\\left|#1\\right|} \\abs{x}",
                "\\cancel{x} + \\bcancel{y} + \\xcancel{z} + \\sout{w}",
                "\\begin{align} a &= b \\\\ c &= d \\end{align}",
                "\\begin{CD} A @>f>> B \\\\ @VgVV @VVhV \\\\ C @>>k> D \\end{CD}",
                "\\fbox{boxed} \\colorbox{yellow}{fill} \\fcolorbox{red}{white}{both}",
                "\\mathrm{d}x \\mathit{abc} \\mathsf{abc} \\mathtt{abc} \\mathfrak{ABC} \\mathscr{ABC}",
                "\\text{Unicode: café ü} \\; x²",
            )

        val symbolCommands =
            listOf(
                "alpha", "beta", "gamma", "delta", "epsilon", "varepsilon", "zeta", "eta", "theta", "vartheta",
                "iota", "kappa", "lambda", "mu", "nu", "xi", "pi", "varpi", "rho", "varrho", "sigma", "varsigma",
                "tau", "upsilon", "phi", "varphi", "chi", "psi", "omega", "Gamma", "Delta", "Theta", "Lambda",
                "Xi", "Pi", "Sigma", "Upsilon", "Phi", "Psi", "Omega",
                "lt", "gt", "leq", "geq", "leqslant", "geqslant", "neq", "nleq", "ngeq", "lesssim", "gtrsim", "in", "notin",
                "ni", "propto", "mid", "nmid", "parallel", "sim", "simeq", "cong", "approx", "asymp", "doteq",
                "triangleq", "equiv", "ll", "gg", "prec", "succ", "preceq", "succeq", "subset", "supset",
                "subseteq", "supseteq", "subsetneq", "supsetneq", "sqsubseteq", "vdash", "dashv", "models",
                "perp", "coloneqq",
                "leftarrow", "rightarrow", "leftrightarrow", "Leftarrow", "Rightarrow", "Leftrightarrow",
                "longrightarrow", "Longrightarrow", "mapsto", "longmapsto", "hookrightarrow", "uparrow",
                "downarrow", "rightleftharpoons", "implies", "iff", "to", "gets",
                "times", "div", "pm", "mp", "cdot", "circ", "bullet", "ast", "star", "oplus", "otimes", "odot",
                "wedge", "vee", "cap", "cup", "setminus", "dagger", "ddagger", "diamond", "bmod",
                "infty", "partial", "nabla", "hbar", "ell", "Re", "Im", "aleph", "forall", "exists", "nexists",
                "emptyset", "varnothing", "neg", "angle", "top", "bot", "prime", "ldots", "cdots", "vdots",
                "ddots", "dots", "triangle", "square", "checkmark", "degree", "imath", "jmath",
                "langle", "rangle", "lceil", "rceil", "lfloor", "rfloor", "lvert", "rvert", "lVert", "rVert",
                "sum", "prod", "coprod", "int", "iint", "iiint", "oint", "oiint", "bigcup", "bigcap", "bigoplus",
                "bigotimes", "bigwedge", "bigvee", "lim", "limsup", "liminf", "max", "min", "sup", "inf", "det",
                "log", "ln", "exp", "sin", "cos", "tan", "arcsin", "sinh", "argmax",
                "quad", "qquad", ",", ":", ";", "!", " ",
                "{", "}", "|", "%", "$", "#", "&", "_",
            )
    }
}
