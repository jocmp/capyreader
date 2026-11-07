package com.jocmp.mallet.math

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Formulas in the shape KaTeX-rendered blogs publish. Every one must lay out, so the reader only
 * falls back to TeX source for genuinely unsupported input.
 */
class KatexCoverageTest {
    @Test
    fun corpusLaysOut() {
        val failures =
            corpus.mapNotNull { tex ->
                MalletMath.layout(tex, fontSize = 20f, display = true).exceptionOrNull()?.let { "$tex -> ${it.message}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun everySymbolCommandHasAGlyph() {
        val failures =
            symbolCommands.mapNotNull { name ->
                MalletMath.layout("\\$name", fontSize = 20f, display = false).exceptionOrNull()?.let { "\\$name -> ${it.message}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun everyFontStyleHasGlyphs() {
        val styles = listOf("mathbf", "mathit", "mathbfit", "mathcal", "mathfrak", "mathbb", "mathsf", "mathtt", "mathrm")
        val failures =
            styles.mapNotNull { style ->
                val tex = "\\$style{ABCXYZ abcxyz 0123}"
                MalletMath.layout(tex, fontSize = 20f, display = false).exceptionOrNull()?.let { "$tex -> ${it.message}" }
            }

        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n"))
        }
    }

    @Test
    fun displayMathIsTallerThanInline() {
        val tex = "\\sum_{i=1}^n i = \\frac{n(n+1)}{2}"
        val display = MalletMath.layout(tex, fontSize = 20f, display = true).getOrThrow()
        val inline = MalletMath.layout(tex, fontSize = 20f, display = false).getOrThrow()

        assertTrue(display.height > inline.height)
    }

    private val corpus =
        listOf(
            // From the discussion's Fibonacci/Binet article.
            "F_0 = 0, F_1 = 1, F_{n+1} = F_n + F_{n-1}",
            "\\begin{pmatrix} F_{n+1} \\\\ F_n \\end{pmatrix} = \\begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\end{pmatrix} \\begin{pmatrix} F_n \\\\ F_{n-1} \\end{pmatrix}",
            "F_n = \\frac{1}{\\sqrt{5}} \\left( \\left( \\frac{1 + \\sqrt{5}}{2} \\right)^n - \\left( \\frac{1 - \\sqrt{5}}{2} \\right)^n \\right)",
            "\\varphi = \\frac{1+\\sqrt5}{2} \\approx 1.618",
            // Calculus and analysis.
            "\\int_0^\\infty e^{-x^2} \\, dx = \\frac{\\sqrt{\\pi}}{2}",
            "\\lim_{x \\to 0} \\frac{\\sin x}{x} = 1",
            "f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}",
            "\\frac{\\partial^2 u}{\\partial t^2} = c^2 \\nabla^2 u",
            "\\oint_C \\mathbf{F} \\cdot d\\mathbf{r} = \\iint_S (\\nabla \\times \\mathbf{F}) \\cdot d\\mathbf{S}",
            "\\sum_{k=0}^{\\infty} \\frac{x^k}{k!} = e^x",
            // Linear algebra and probability.
            "A = U \\Sigma V^\\top",
            "\\det \\begin{vmatrix} a & b \\\\ c & d \\end{vmatrix} = ad - bc",
            "\\mathbb{E}[X] = \\sum_{x} x \\, \\Pr(X = x)",
            "P(A \\mid B) = \\frac{P(B \\mid A) P(A)}{P(B)}",
            "\\mathcal{N}(\\mu, \\sigma^2)",
            "\\| x \\|_2 = \\sqrt{\\sum_i x_i^2}",
            "\\langle u, v \\rangle = \\sum_{i=1}^n u_i \\bar{v}_i",
            // Machine learning.
            "\\hat{y} = \\operatorname*{arg\\,max}_{y} p(y \\mid x)",
            "\\mathcal{L}(\\theta) = -\\frac{1}{N} \\sum_{i=1}^{N} \\log p_\\theta(y_i \\mid x_i)",
            "\\text{softmax}(z)_i = \\frac{e^{z_i}}{\\sum_j e^{z_j}}",
            "\\theta_{t+1} = \\theta_t - \\eta \\nabla_\\theta J(\\theta_t)",
            // Environments.
            "|x| = \\begin{cases} x & \\text{if } x \\geq 0 \\\\ -x & \\text{otherwise} \\end{cases}",
            "\\begin{aligned} (a+b)^2 &= a^2 + 2ab + b^2 \\\\ &\\geq 4ab \\end{aligned}",
            "\\begin{align*} x &= 1 & y &= 2 \\end{align*}",
            "\\begin{bmatrix} 1 & 0 \\\\ 0 & 1 \\end{bmatrix}",
            "\\begin{array}{c|c} a & b \\\\ \\hline c & d \\end{array}",
            "\\begin{gathered} a = b \\\\ c = d \\end{gathered}",
            // Number theory and logic.
            "a \\equiv b \\pmod{n}",
            "\\forall \\epsilon > 0 \\; \\exists \\delta > 0",
            "\\neg (p \\land q) \\iff \\neg p \\lor \\neg q",
            "\\gcd(a, b) = \\gcd(b, a \\bmod b)",
            "\\binom{n}{k} = \\frac{n!}{k!(n-k)!}",
            "\\lfloor x \\rfloor \\leq x < \\lfloor x \\rfloor + 1",
            "x \\in \\mathbb{R}^n, \\quad A \\subseteq B",
            "\\boxed{E = mc^2}",
            // From Gundersen's inverse covariance post.
            "\\boldsymbol{\\Sigma}^k = \\underbrace{\\left( \\mathbf{Q} \\boldsymbol{\\Lambda} \\mathbf{Q}^{\\top} \\right) \\dots \\left( \\mathbf{Q} \\boldsymbol{\\Lambda} \\mathbf{Q}^{\\top} \\right)}_{\\text{\$k\$ times}} = \\mathbf{Q} \\boldsymbol{\\Lambda}^k \\mathbf{Q}^{\\top}. \\tag{5}",
            "\\overbrace{1 + 1 + \\cdots + 1}^{n}",
            "\\overset{\\text{def}}{=}",
            "a \\xrightarrow{f} b",
            "\\bigl( \\Bigl[ \\biggl\\{ x \\biggr\\} \\Bigr] \\bigr)",
            "\\left\\lVert \\frac{a}{b} \\right\\rVert",
            "\\vec{v} = \\dot{x} \\hat{i} + \\ddot{y} \\tilde{j}",
            "\\overline{z} = \\underline{w}",
            "\\color{blue} x^2 + \\textcolor{red}{y}",
            "\\displaystyle \\sum_{n=1}^\\infty \\frac{1}{n^2} = \\frac{\\pi^2}{6} \\tag{1}",
            "\\sqrt[3]{x} \\cdot \\sqrt[n]{y}",
            "x \\not\\in A, \\; y \\neq z",
            "\\dfrac{1}{2} + \\tfrac{3}{4}",
            "\\cdots \\ldots \\vdots \\ddots",
            "100\\% \\; \\$5 \\; \\#1",
        )

    private val symbolCommands =
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
            "sum", "prod", "coprod", "int", "iint", "iiint", "oint", "bigcup", "bigcap", "bigoplus",
            "bigotimes", "bigwedge", "bigvee", "lim", "limsup", "liminf", "max", "min", "sup", "inf", "det",
            "log", "ln", "exp", "sin", "cos", "tan", "arcsin", "sinh", "argmax",
            "quad", "qquad", ",", ":", ";", "!", " ",
            "{", "}", "|", "%", "$", "#", "&", "_",
        )
}
