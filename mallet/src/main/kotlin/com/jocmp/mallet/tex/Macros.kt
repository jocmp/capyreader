package com.jocmp.mallet.tex

object Macros {
    val builtins: Map<String, MacroDefinition> = LinkedHashMap<String, MacroDefinition>().apply {
        defineMacroTools()
        defineLatexSymbols()
        defineAmsmath()
        defineLatexSource2e()
        defineMathtools()
        defineMathmlAlternates()
        defineStmaryrd()
        defineTexvc()
        defineStatmath()
        defineBraket()
        defineActuarialAngle()
        defineKhanAcademyColors()
        defineFunctionMacros()
    }
}

private fun MutableMap<String, MacroDefinition>.defineMacroTools() {
    define("\\noexpand") { context ->
        val t = context.popToken()
        if (context.isExpandable(t.text)) {
            t.noexpand = true
            t.treatAsRelax = true
        }
        MacroExpansion(tokens = listOf(t), numArgs = 0)
    }
    define("\\expandafter") { context ->
        val t = context.popToken()
        context.expandOnce(true)
        MacroExpansion(tokens = listOf(t), numArgs = 0)
    }
    define("\\@firstoftwo") { context ->
        val args = context.consumeArgs(2)
        MacroExpansion(tokens = args[0], numArgs = 0)
    }
    define("\\@secondoftwo") { context ->
        val args = context.consumeArgs(2)
        MacroExpansion(tokens = args[1], numArgs = 0)
    }
    define("\\@ifnextchar") { context ->
        val args = context.consumeArgs(3)
        context.consumeSpaces()
        val nextToken = context.future()
        if (args[0].size == 1 && args[0][0].text == nextToken.text) {
            MacroExpansion(tokens = args[1], numArgs = 0)
        } else {
            MacroExpansion(tokens = args[2], numArgs = 0)
        }
    }
    define("\\@ifstar", "\\@ifnextchar *{\\@firstoftwo{#1}}")
    define("\\TextOrMath") { context ->
        val args = context.consumeArgs(2)
        if (context.mode == Mode.TEXT) {
            MacroExpansion(tokens = args[0], numArgs = 0)
        } else {
            MacroExpansion(tokens = args[1], numArgs = 0)
        }
    }
    define("\\char", ::charMacro)
    define("\\newcommand") { context -> newcommand(context, existsOK = false, nonexistsOK = true, skipIfExists = false) }
    define("\\renewcommand") { context -> newcommand(context, existsOK = true, nonexistsOK = false, skipIfExists = false) }
    define("\\providecommand") { context -> newcommand(context, existsOK = true, nonexistsOK = true, skipIfExists = true) }
    define("\\message") { context ->
        context.consumeArgs(1)
        MacroString("")
    }
    define("\\errmessage") { context ->
        context.consumeArgs(1)
        MacroString("")
    }
    define("\\show") { context ->
        context.popToken()
        MacroString("")
    }
}

private fun MutableMap<String, MacroDefinition>.defineLatexSymbols() {
    define("\\bgroup", "{")
    define("\\egroup", "}")
    define("~", "\\nobreakspace")
    define("\\lq", "`")
    define("\\rq", "'")
    define("\\aa", "\\r a")
    define("\\AA", "\\r A")
    define("\\textcopyright", "\\html@mathml{\\textcircled{c}}{\\char`\u00A9}")
    define("\\copyright", "\\TextOrMath{\\textcopyright}{\\text{\\textcopyright}}")
    define("\\textregistered", "\\html@mathml{\\textcircled{\\scriptsize R}}{\\char`\u00AE}")
    define("\u212C", "\\mathscr{B}")
    define("\u2130", "\\mathscr{E}")
    define("\u2131", "\\mathscr{F}")
    define("\u210B", "\\mathscr{H}")
    define("\u2110", "\\mathscr{I}")
    define("\u2112", "\\mathscr{L}")
    define("\u2133", "\\mathscr{M}")
    define("\u211B", "\\mathscr{R}")
    define("\u212D", "\\mathfrak{C}")
    define("\u210C", "\\mathfrak{H}")
    define("\u2128", "\\mathfrak{Z}")
    define("\\Bbbk", "\\Bbb{k}")
    define("\\llap", "\\mathllap{\\textrm{#1}}")
    define("\\rlap", "\\mathrlap{\\textrm{#1}}")
    define("\\clap", "\\mathclap{\\textrm{#1}}")
    define("\\mathstrut", "\\vphantom{(}")
    define("\\underbar", "\\underline{\\text{#1}}")
    define("\\not", "\\html@mathml{\\mathrel{\\mathrlap\\@not}\\nobreak}{\\char\"338}")
    define("\\neq", "\\html@mathml{\\mathrel{\\not=}}{\\mathrel{\\char`\u2260}}")
    define("\\ne", "\\neq")
    define("\u2260", "\\neq")
    define("\\notin", "\\html@mathml{\\mathrel{{\\in}\\mathllap{/\\mskip1mu}}}{\\mathrel{\\char`\u2209}}")
    define("\u2209", "\\notin")
    define("\u2258", "\\html@mathml{\\mathrel{=\\kern{-1em}\\raisebox{0.4em}{\$\\scriptsize\\frown\$}}}{\\mathrel{\\char`\u2258}}")
    define("\u2259", "\\html@mathml{\\stackrel{\\tiny\\wedge}{=}}{\\mathrel{\\char`\u2258}}")
    define("\u225A", "\\html@mathml{\\stackrel{\\tiny\\vee}{=}}{\\mathrel{\\char`\u225A}}")
    define("\u225B", "\\html@mathml{\\stackrel{\\scriptsize\\star}{=}}{\\mathrel{\\char`\u225B}}")
    define("\u225D", "\\html@mathml{\\stackrel{\\tiny\\mathrm{def}}{=}}{\\mathrel{\\char`\u225D}}")
    define("\u225E", "\\html@mathml{\\stackrel{\\tiny\\mathrm{m}}{=}}{\\mathrel{\\char`\u225E}}")
    define("\u225F", "\\html@mathml{\\stackrel{\\tiny?}{=}}{\\mathrel{\\char`\u225F}}")
    define("\u27C2", "\\perp")
    define("\u203C", "\\mathclose{!\\mkern-0.8mu!}")
    define("\u220C", "\\notni")
    define("\u231C", "\\ulcorner")
    define("\u231D", "\\urcorner")
    define("\u231E", "\\llcorner")
    define("\u231F", "\\lrcorner")
    define("\u00A9", "\\copyright")
    define("\u00AE", "\\textregistered")
    define("\\ulcorner", "\\html@mathml{\\@ulcorner}{\\mathop{\\char\"231c}}")
    define("\\urcorner", "\\html@mathml{\\@urcorner}{\\mathop{\\char\"231d}}")
    define("\\llcorner", "\\html@mathml{\\@llcorner}{\\mathop{\\char\"231e}}")
    define("\\lrcorner", "\\html@mathml{\\@lrcorner}{\\mathop{\\char\"231f}}")
    define("\\vdots", "{\\varvdots\\rule{0pt}{15pt}}")
    define("\u22EE", "\\vdots")
}

private fun MutableMap<String, MacroDefinition>.defineAmsmath() {
    define("\\varGamma", "\\mathit{\\Gamma}")
    define("\\varDelta", "\\mathit{\\Delta}")
    define("\\varTheta", "\\mathit{\\Theta}")
    define("\\varLambda", "\\mathit{\\Lambda}")
    define("\\varXi", "\\mathit{\\Xi}")
    define("\\varPi", "\\mathit{\\Pi}")
    define("\\varSigma", "\\mathit{\\Sigma}")
    define("\\varUpsilon", "\\mathit{\\Upsilon}")
    define("\\varPhi", "\\mathit{\\Phi}")
    define("\\varPsi", "\\mathit{\\Psi}")
    define("\\varOmega", "\\mathit{\\Omega}")
    define("\\substack", "\\begin{subarray}{c}#1\\end{subarray}")
    define("\\colon", "\\nobreak\\mskip2mu\\mathpunct{}\\mathchoice{\\mkern-3mu}{\\mkern-3mu}{}{}{:}\\mskip6mu\\relax")
    define("\\boxed", "\\fbox{\$\\displaystyle{#1}\$}")
    define("\\iff", "\\DOTSB\\;\\Longleftrightarrow\\;")
    define("\\implies", "\\DOTSB\\;\\Longrightarrow\\;")
    define("\\impliedby", "\\DOTSB\\;\\Longleftarrow\\;")
    define("\\dddot", "{\\overset{\\raisebox{-0.1ex}{\\normalsize ...}}{#1}}")
    define("\\ddddot", "{\\overset{\\raisebox{-0.1ex}{\\normalsize ....}}{#1}}")
    define("\\dots") { context ->
        var thedots = "\\dotso"
        val next = context.expandAfterFuture().text
        val byToken = dotsByToken[next]
        if (byToken != null) {
            thedots = byToken
        } else if (next.startsWith("\\not")) {
            thedots = "\\dotsb"
        } else {
            val symbol = Symbols.math[next]
            if (symbol != null && dotsbGroups.contains(symbol.group)) {
                thedots = "\\dotsb"
            }
        }
        MacroString(thedots)
    }
    define("\\dotso") { context ->
        val next = context.future().text
        if (next in spaceAfterDots) {
            MacroString("\\ldots\\,")
        } else {
            MacroString("\\ldots")
        }
    }
    define("\\dotsc") { context ->
        val next = context.future().text
        if (next in spaceAfterDots && next != ",") {
            MacroString("\\ldots\\,")
        } else {
            MacroString("\\ldots")
        }
    }
    define("\\cdots") { context ->
        val next = context.future().text
        if (next in spaceAfterDots) {
            MacroString("\\@cdots\\,")
        } else {
            MacroString("\\@cdots")
        }
    }
    define("\\dotsb", "\\cdots")
    define("\\dotsm", "\\cdots")
    define("\\dotsi", "\\!\\cdots")
    define("\\dotsx", "\\ldots\\,")
    define("\\DOTSI", "\\relax")
    define("\\DOTSB", "\\relax")
    define("\\DOTSX", "\\relax")
    define("\\tmspace", "\\TextOrMath{\\kern#1#3}{\\mskip#1#2}\\relax")
    define("\\,", "\\tmspace+{3mu}{.1667em}")
    define("\\thinspace", "\\,")
    define("\\>", "\\mskip{4mu}")
    define("\\:", "\\tmspace+{4mu}{.2222em}")
    define("\\medspace", "\\:")
    define("\\;", "\\tmspace+{5mu}{.2777em}")
    define("\\thickspace", "\\;")
    define("\\!", "\\tmspace-{3mu}{.1667em}")
    define("\\negthinspace", "\\!")
    define("\\negmedspace", "\\tmspace-{4mu}{.2222em}")
    define("\\negthickspace", "\\tmspace-{5mu}{.277em}")
    define("\\enspace", "\\kern.5em ")
    define("\\enskip", "\\hskip.5em\\relax")
    define("\\quad", "\\hskip1em\\relax")
    define("\\qquad", "\\hskip2em\\relax")
    define("\\tag", "\\@ifstar\\tag@literal\\tag@paren")
    define("\\tag@paren", "\\tag@literal{({#1})}")
    define("\\tag@literal") { context ->
        if (isTruthy(context.macros.get("\\df@tag"))) {
            throw ParseError("Multiple \\tag")
        }
        MacroString("\\gdef\\df@tag{\\text{#1}}")
    }
    define("\\bmod", "\\mathchoice{\\mskip1mu}{\\mskip1mu}{\\mskip5mu}{\\mskip5mu}\\mathbin{\\rm mod}\\mathchoice{\\mskip1mu}{\\mskip1mu}{\\mskip5mu}{\\mskip5mu}")
    define("\\pod", "\\allowbreak\\mathchoice{\\mkern18mu}{\\mkern8mu}{\\mkern8mu}{\\mkern8mu}(#1)")
    define("\\pmod", "\\pod{{\\rm mod}\\mkern6mu#1}")
    define("\\mod", "\\allowbreak\\mathchoice{\\mkern18mu}{\\mkern12mu}{\\mkern12mu}{\\mkern12mu}{\\rm mod}\\,\\,#1")
}

private fun MutableMap<String, MacroDefinition>.defineLatexSource2e() {
    define("\\newline", "\\\\\\relax")
    define("\\TeX", "\\textrm{\\html@mathml{T\\kern-.1667em\\raisebox{-.5ex}{E}\\kern-.125emX}{TeX}}")
    define("\\LaTeX", "\\textrm{\\html@mathml{L\\kern-.36em\\raisebox{0.205em}{\\scriptstyle A}\\kern-.15em\\TeX}{LaTeX}}")
    define("\\KaTeX", "\\textrm{\\html@mathml{K\\kern-.17em\\raisebox{0.205em}{\\scriptstyle A}\\kern-.15em\\TeX}{KaTeX}}")
    define("\\hspace", "\\@ifstar\\@hspacer\\@hspace")
    define("\\@hspace", "\\hskip #1\\relax")
    define("\\@hspacer", "\\rule{0pt}{0pt}\\hskip #1\\relax")
}

private fun MutableMap<String, MacroDefinition>.defineMathtools() {
    define("\\ordinarycolon", ":")
    define("\\vcentcolon", "\\mathrel{\\mathop\\ordinarycolon}")
    define("\\dblcolon", "\\html@mathml{\\mathrel{\\vcentcolon\\mathrel{\\mkern-.9mu}\\vcentcolon}}{\\mathop{\\char\"2237}}")
    define("\\coloneqq", "\\html@mathml{\\mathrel{\\vcentcolon\\mathrel{\\mkern-1.2mu}=}}{\\mathop{\\char\"2254}}")
    define("\\Coloneqq", "\\html@mathml{\\mathrel{\\dblcolon\\mathrel{\\mkern-1.2mu}=}}{\\mathop{\\char\"2237\\char\"3d}}")
    define("\\coloneq", "\\html@mathml{\\mathrel{\\vcentcolon\\mathrel{\\mkern-1.2mu}\\mathrel{-}}}{\\mathop{\\char\"3a\\char\"2212}}")
    define("\\Coloneq", "\\html@mathml{\\mathrel{\\dblcolon\\mathrel{\\mkern-1.2mu}\\mathrel{-}}}{\\mathop{\\char\"2237\\char\"2212}}")
    define("\\eqqcolon", "\\html@mathml{\\mathrel{=\\mathrel{\\mkern-1.2mu}\\vcentcolon}}{\\mathop{\\char\"2255}}")
    define("\\Eqqcolon", "\\html@mathml{\\mathrel{=\\mathrel{\\mkern-1.2mu}\\dblcolon}}{\\mathop{\\char\"3d\\char\"2237}}")
    define("\\eqcolon", "\\html@mathml{\\mathrel{\\mathrel{-}\\mathrel{\\mkern-1.2mu}\\vcentcolon}}{\\mathop{\\char\"2239}}")
    define("\\Eqcolon", "\\html@mathml{\\mathrel{\\mathrel{-}\\mathrel{\\mkern-1.2mu}\\dblcolon}}{\\mathop{\\char\"2212\\char\"2237}}")
    define("\\colonapprox", "\\html@mathml{\\mathrel{\\vcentcolon\\mathrel{\\mkern-1.2mu}\\approx}}{\\mathop{\\char\"3a\\char\"2248}}")
    define("\\Colonapprox", "\\html@mathml{\\mathrel{\\dblcolon\\mathrel{\\mkern-1.2mu}\\approx}}{\\mathop{\\char\"2237\\char\"2248}}")
    define("\\colonsim", "\\html@mathml{\\mathrel{\\vcentcolon\\mathrel{\\mkern-1.2mu}\\sim}}{\\mathop{\\char\"3a\\char\"223c}}")
    define("\\Colonsim", "\\html@mathml{\\mathrel{\\dblcolon\\mathrel{\\mkern-1.2mu}\\sim}}{\\mathop{\\char\"2237\\char\"223c}}")
    define("\u2237", "\\dblcolon")
    define("\u2239", "\\eqcolon")
    define("\u2254", "\\coloneqq")
    define("\u2255", "\\eqqcolon")
    define("\u2A74", "\\Coloneqq")
    define("\\ratio", "\\vcentcolon")
    define("\\coloncolon", "\\dblcolon")
    define("\\colonequals", "\\coloneqq")
    define("\\coloncolonequals", "\\Coloneqq")
    define("\\equalscolon", "\\eqqcolon")
    define("\\equalscoloncolon", "\\Eqqcolon")
    define("\\colonminus", "\\coloneq")
    define("\\coloncolonminus", "\\Coloneq")
    define("\\minuscolon", "\\eqcolon")
    define("\\minuscoloncolon", "\\Eqcolon")
    define("\\coloncolonapprox", "\\Colonapprox")
    define("\\coloncolonsim", "\\Colonsim")
    define("\\simcolon", "\\mathrel{\\sim\\mathrel{\\mkern-1.2mu}\\vcentcolon}")
    define("\\simcoloncolon", "\\mathrel{\\sim\\mathrel{\\mkern-1.2mu}\\dblcolon}")
    define("\\approxcolon", "\\mathrel{\\approx\\mathrel{\\mkern-1.2mu}\\vcentcolon}")
    define("\\approxcoloncolon", "\\mathrel{\\approx\\mathrel{\\mkern-1.2mu}\\dblcolon}")
    define("\\notni", "\\html@mathml{\\not\\ni}{\\mathrel{\\char`\u220C}}")
    define("\\limsup", "\\DOTSB\\operatorname*{lim\\,sup}")
    define("\\liminf", "\\DOTSB\\operatorname*{lim\\,inf}")
    define("\\injlim", "\\DOTSB\\operatorname*{inj\\,lim}")
    define("\\projlim", "\\DOTSB\\operatorname*{proj\\,lim}")
    define("\\varlimsup", "\\DOTSB\\operatorname*{\\overline{lim}}")
    define("\\varliminf", "\\DOTSB\\operatorname*{\\underline{lim}}")
    define("\\varinjlim", "\\DOTSB\\operatorname*{\\underrightarrow{lim}}")
    define("\\varprojlim", "\\DOTSB\\operatorname*{\\underleftarrow{lim}}")
}

private fun MutableMap<String, MacroDefinition>.defineMathmlAlternates() {
    define("\\gvertneqq", "\\html@mathml{\\@gvertneqq}{\u2269}")
    define("\\lvertneqq", "\\html@mathml{\\@lvertneqq}{\u2268}")
    define("\\ngeqq", "\\html@mathml{\\@ngeqq}{\u2271}")
    define("\\ngeqslant", "\\html@mathml{\\@ngeqslant}{\u2271}")
    define("\\nleqq", "\\html@mathml{\\@nleqq}{\u2270}")
    define("\\nleqslant", "\\html@mathml{\\@nleqslant}{\u2270}")
    define("\\nshortmid", "\\html@mathml{\\@nshortmid}{\u2224}")
    define("\\nshortparallel", "\\html@mathml{\\@nshortparallel}{\u2226}")
    define("\\nsubseteqq", "\\html@mathml{\\@nsubseteqq}{\u2288}")
    define("\\nsupseteqq", "\\html@mathml{\\@nsupseteqq}{\u2289}")
    define("\\varsubsetneq", "\\html@mathml{\\@varsubsetneq}{\u228A}")
    define("\\varsubsetneqq", "\\html@mathml{\\@varsubsetneqq}{\u2ACB}")
    define("\\varsupsetneq", "\\html@mathml{\\@varsupsetneq}{\u228B}")
    define("\\varsupsetneqq", "\\html@mathml{\\@varsupsetneqq}{\u2ACC}")
    define("\\imath", "\\html@mathml{\\@imath}{\u0131}")
    define("\\jmath", "\\html@mathml{\\@jmath}{\u0237}")
}

private fun MutableMap<String, MacroDefinition>.defineStmaryrd() {
    define("\\llbracket", "\\html@mathml{\\mathopen{[\\mkern-3.2mu[}}{\\mathopen{\\char`\u27E6}}")
    define("\\rrbracket", "\\html@mathml{\\mathclose{]\\mkern-3.2mu]}}{\\mathclose{\\char`\u27E7}}")
    define("\u27E6", "\\llbracket")
    define("\u27E7", "\\rrbracket")
    define("\\lBrace", "\\html@mathml{\\mathopen{\\{\\mkern-3.2mu[}}{\\mathopen{\\char`\u2983}}")
    define("\\rBrace", "\\html@mathml{\\mathclose{]\\mkern-3.2mu\\}}}{\\mathclose{\\char`\u2984}}")
    define("\u21A4", "\\mapsfrom")
    define("\\mapsfrom", "\\html@mathml{\\mathrel{\\mathreflectbox{\\mapsto}}}{\\mathrel{\\char`\u21A4}}")
    define("\u2983", "\\lBrace")
    define("\u2984", "\\rBrace")
    define("\\minuso", "\\mathbin{\\html@mathml{{\\mathrlap{\\mathchoice{\\kern{0.145em}}{\\kern{0.145em}}{\\kern{0.1015em}}{\\kern{0.0725em}}\\circ}{-}}}{\\char`\u29B5}}")
    define("\u29B5", "\\minuso")
}

private fun MutableMap<String, MacroDefinition>.defineTexvc() {
    define("\\darr", "\\downarrow")
    define("\\dArr", "\\Downarrow")
    define("\\Darr", "\\Downarrow")
    define("\\lang", "\\langle")
    define("\\rang", "\\rangle")
    define("\\uarr", "\\uparrow")
    define("\\uArr", "\\Uparrow")
    define("\\Uarr", "\\Uparrow")
    define("\\N", "\\mathbb{N}")
    define("\\R", "\\mathbb{R}")
    define("\\Z", "\\mathbb{Z}")
    define("\\alef", "\\aleph")
    define("\\alefsym", "\\aleph")
    define("\\Alpha", "\\mathrm{A}")
    define("\\Beta", "\\mathrm{B}")
    define("\\bull", "\\bullet")
    define("\\Chi", "\\mathrm{X}")
    define("\\clubs", "\\clubsuit")
    define("\\cnums", "\\mathbb{C}")
    define("\\Complex", "\\mathbb{C}")
    define("\\Dagger", "\\ddagger")
    define("\\diamonds", "\\diamondsuit")
    define("\\empty", "\\emptyset")
    define("\\Epsilon", "\\mathrm{E}")
    define("\\Eta", "\\mathrm{H}")
    define("\\exist", "\\exists")
    define("\\harr", "\\leftrightarrow")
    define("\\hArr", "\\Leftrightarrow")
    define("\\Harr", "\\Leftrightarrow")
    define("\\hearts", "\\heartsuit")
    define("\\image", "\\Im")
    define("\\infin", "\\infty")
    define("\\Iota", "\\mathrm{I}")
    define("\\isin", "\\in")
    define("\\Kappa", "\\mathrm{K}")
    define("\\larr", "\\leftarrow")
    define("\\lArr", "\\Leftarrow")
    define("\\Larr", "\\Leftarrow")
    define("\\lrarr", "\\leftrightarrow")
    define("\\lrArr", "\\Leftrightarrow")
    define("\\Lrarr", "\\Leftrightarrow")
    define("\\Mu", "\\mathrm{M}")
    define("\\natnums", "\\mathbb{N}")
    define("\\Nu", "\\mathrm{N}")
    define("\\Omicron", "\\mathrm{O}")
    define("\\plusmn", "\\pm")
    define("\\rarr", "\\rightarrow")
    define("\\rArr", "\\Rightarrow")
    define("\\Rarr", "\\Rightarrow")
    define("\\real", "\\Re")
    define("\\reals", "\\mathbb{R}")
    define("\\Reals", "\\mathbb{R}")
    define("\\Rho", "\\mathrm{P}")
    define("\\sdot", "\\cdot")
    define("\\sect", "\\S")
    define("\\spades", "\\spadesuit")
    define("\\sub", "\\subset")
    define("\\sube", "\\subseteq")
    define("\\supe", "\\supseteq")
    define("\\Tau", "\\mathrm{T}")
    define("\\thetasym", "\\vartheta")
    define("\\weierp", "\\wp")
    define("\\Zeta", "\\mathrm{Z}")
}

private fun MutableMap<String, MacroDefinition>.defineStatmath() {
    define("\\argmin", "\\DOTSB\\operatorname*{arg\\,min}")
    define("\\argmax", "\\DOTSB\\operatorname*{arg\\,max}")
    define("\\plim", "\\DOTSB\\mathop{\\operatorname{plim}}\\limits")
}

private fun MutableMap<String, MacroDefinition>.defineBraket() {
    define("\\bra", "\\mathinner{\\langle{#1}|}")
    define("\\ket", "\\mathinner{|{#1}\\rangle}")
    define("\\braket", "\\mathinner{\\langle{#1}\\rangle}")
    define("\\Bra", "\\left\\langle#1\\right|")
    define("\\Ket", "\\left|#1\\right\\rangle")
    define("\\bra@ket", braketHelper(one = false))
    define("\\bra@set", braketHelper(one = true))
    define("\\Braket", "\\bra@ket{\\left\\langle}{\\,\\middle\\vert\\,}{\\,\\middle\\vert\\,}{\\right\\rangle}")
    define("\\Set", "\\bra@set{\\left\\{\\:}{\\;\\middle\\vert\\;}{\\;\\middle\\Vert\\;}{\\:\\right\\}}")
    define("\\set", "\\bra@set{\\{\\,}{\\mid}{}{\\,\\}}")
}

private fun MutableMap<String, MacroDefinition>.defineActuarialAngle() {
    define("\\angln", "{\\angl n}")
}

// Defined alongside their functions in KaTeX: operatorname.ts, phantom.ts, and array.ts.
private fun MutableMap<String, MacroDefinition>.defineFunctionMacros() {
    define("\\operatorname", "\\@ifstar\\operatornamewithlimits\\operatorname@")
    define("\\hphantom", "\\smash{\\phantom{#1}}")
    define("\\nonumber", "\\gdef\\@eqnsw{0}")
    define("\\notag", "\\nonumber")
}

private fun MutableMap<String, MacroDefinition>.defineKhanAcademyColors() {
    define("\\blue", "\\textcolor{##6495ed}{#1}")
    define("\\orange", "\\textcolor{##ffa500}{#1}")
    define("\\pink", "\\textcolor{##ff00af}{#1}")
    define("\\red", "\\textcolor{##df0030}{#1}")
    define("\\green", "\\textcolor{##28ae7b}{#1}")
    define("\\gray", "\\textcolor{gray}{#1}")
    define("\\purple", "\\textcolor{##9d38bd}{#1}")
    define("\\blueA", "\\textcolor{##ccfaff}{#1}")
    define("\\blueB", "\\textcolor{##80f6ff}{#1}")
    define("\\blueC", "\\textcolor{##63d9ea}{#1}")
    define("\\blueD", "\\textcolor{##11accd}{#1}")
    define("\\blueE", "\\textcolor{##0c7f99}{#1}")
    define("\\tealA", "\\textcolor{##94fff5}{#1}")
    define("\\tealB", "\\textcolor{##26edd5}{#1}")
    define("\\tealC", "\\textcolor{##01d1c1}{#1}")
    define("\\tealD", "\\textcolor{##01a995}{#1}")
    define("\\tealE", "\\textcolor{##208170}{#1}")
    define("\\greenA", "\\textcolor{##b6ffb0}{#1}")
    define("\\greenB", "\\textcolor{##8af281}{#1}")
    define("\\greenC", "\\textcolor{##74cf70}{#1}")
    define("\\greenD", "\\textcolor{##1fab54}{#1}")
    define("\\greenE", "\\textcolor{##0d923f}{#1}")
    define("\\goldA", "\\textcolor{##ffd0a9}{#1}")
    define("\\goldB", "\\textcolor{##ffbb71}{#1}")
    define("\\goldC", "\\textcolor{##ff9c39}{#1}")
    define("\\goldD", "\\textcolor{##e07d10}{#1}")
    define("\\goldE", "\\textcolor{##a75a05}{#1}")
    define("\\redA", "\\textcolor{##fca9a9}{#1}")
    define("\\redB", "\\textcolor{##ff8482}{#1}")
    define("\\redC", "\\textcolor{##f9685d}{#1}")
    define("\\redD", "\\textcolor{##e84d39}{#1}")
    define("\\redE", "\\textcolor{##bc2612}{#1}")
    define("\\maroonA", "\\textcolor{##ffbde0}{#1}")
    define("\\maroonB", "\\textcolor{##ff92c6}{#1}")
    define("\\maroonC", "\\textcolor{##ed5fa6}{#1}")
    define("\\maroonD", "\\textcolor{##ca337c}{#1}")
    define("\\maroonE", "\\textcolor{##9e034e}{#1}")
    define("\\purpleA", "\\textcolor{##ddd7ff}{#1}")
    define("\\purpleB", "\\textcolor{##c6b9fc}{#1}")
    define("\\purpleC", "\\textcolor{##aa87ff}{#1}")
    define("\\purpleD", "\\textcolor{##7854ab}{#1}")
    define("\\purpleE", "\\textcolor{##543b78}{#1}")
    define("\\mintA", "\\textcolor{##f5f9e8}{#1}")
    define("\\mintB", "\\textcolor{##edf2df}{#1}")
    define("\\mintC", "\\textcolor{##e0e5cc}{#1}")
    define("\\grayA", "\\textcolor{##f6f7f7}{#1}")
    define("\\grayB", "\\textcolor{##f0f1f2}{#1}")
    define("\\grayC", "\\textcolor{##e3e5e6}{#1}")
    define("\\grayD", "\\textcolor{##d6d8da}{#1}")
    define("\\grayE", "\\textcolor{##babec2}{#1}")
    define("\\grayF", "\\textcolor{##888d93}{#1}")
    define("\\grayG", "\\textcolor{##626569}{#1}")
    define("\\grayH", "\\textcolor{##3b3e40}{#1}")
    define("\\grayI", "\\textcolor{##21242c}{#1}")
    define("\\kaBlue", "\\textcolor{##314453}{#1}")
    define("\\kaGreen", "\\textcolor{##71B307}{#1}")
}

private fun MutableMap<String, MacroDefinition>.define(name: String, body: String) {
    this[name] = MacroString(body)
}

private fun MutableMap<String, MacroDefinition>.define(name: String, body: (MacroContext) -> MacroDefinition) {
    this[name] = MacroFunction(body)
}

private val digitToNumber = mapOf(
    "0" to 0, "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7, "8" to 8,
    "9" to 9, "a" to 10, "A" to 10, "b" to 11, "B" to 11, "c" to 12, "C" to 12,
    "d" to 13, "D" to 13, "e" to 14, "E" to 14, "f" to 15, "F" to 15,
)

private fun charMacro(context: MacroContext): MacroDefinition {
    var token = context.popToken()
    var base: Int? = null
    var number: Long? = 0
    when (token.text) {
        "'" -> {
            base = 8
            token = context.popToken()
        }
        "\"" -> {
            base = 16
            token = context.popToken()
        }
        "`" -> {
            token = context.popToken()
            number = if (token.text.startsWith("\\")) {
                token.text.getOrNull(1)?.code?.toLong()
            } else if (token.text == "EOF") {
                throw ParseError("\\char` missing argument")
            } else {
                token.text.firstOrNull()?.code?.toLong()
            }
        }
        else -> base = 10
    }
    if (base != null) {
        val first = digitToNumber[token.text]
        if (first == null || first >= base) {
            throw ParseError("Invalid base-$base digit ${token.text}")
        }
        var value = first.toLong()
        var digit = digitToNumber[context.future().text]
        while (digit != null && digit < base) {
            value = value * base + digit
            context.popToken()
            digit = digitToNumber[context.future().text]
        }
        number = value
    }
    return MacroString("\\@char{${number ?: "NaN"}}")
}

private val numArgsRegex = Regex("^\\s*[0-9]+\\s*$")

private fun newcommand(
    context: MacroContext,
    existsOK: Boolean,
    nonexistsOK: Boolean,
    skipIfExists: Boolean,
): MacroDefinition {
    var arg = context.consumeArg().tokens
    if (arg.size != 1) {
        throw ParseError("\\newcommand's first argument must be a macro name")
    }
    val name = arg[0].text

    val exists = context.isDefined(name)
    if (exists && !existsOK) {
        throw ParseError("\\newcommand{$name} attempting to redefine $name; use \\renewcommand")
    }
    if (!exists && !nonexistsOK) {
        throw ParseError("\\renewcommand{$name} when command $name does not yet exist; use \\newcommand")
    }

    var numArgs = 0
    arg = context.consumeArg().tokens
    if (arg.size == 1 && arg[0].text == "[") {
        var argText = ""
        var token = context.expandNextToken()
        while (token.text != "]" && token.text != "EOF") {
            argText += token.text
            token = context.expandNextToken()
        }
        if (!numArgsRegex.containsMatchIn(argText)) {
            throw ParseError("Invalid number of arguments: $argText")
        }
        // Deviation: JS parseInt never overflows; reject counts that don't fit in an Int.
        numArgs = argText.trim().toIntOrNull() ?: throw ParseError("Invalid number of arguments: $argText")
        arg = context.consumeArg().tokens
    }

    if (!(exists && skipIfExists)) {
        context.macros.set(name, MacroExpansion(tokens = arg, numArgs = numArgs))
    }
    return MacroString("")
}

private val dotsByToken = mapOf(
    "," to "\\dotsc",
    "\\not" to "\\dotsb",
    "+" to "\\dotsb",
    "=" to "\\dotsb",
    "<" to "\\dotsb",
    ">" to "\\dotsb",
    "-" to "\\dotsb",
    "*" to "\\dotsb",
    ":" to "\\dotsb",
    "\\DOTSB" to "\\dotsb",
    "\\coprod" to "\\dotsb",
    "\\bigvee" to "\\dotsb",
    "\\bigwedge" to "\\dotsb",
    "\\biguplus" to "\\dotsb",
    "\\bigcap" to "\\dotsb",
    "\\bigcup" to "\\dotsb",
    "\\prod" to "\\dotsb",
    "\\sum" to "\\dotsb",
    "\\bigotimes" to "\\dotsb",
    "\\bigoplus" to "\\dotsb",
    "\\bigodot" to "\\dotsb",
    "\\bigsqcup" to "\\dotsb",
    "\\And" to "\\dotsb",
    "\\longrightarrow" to "\\dotsb",
    "\\Longrightarrow" to "\\dotsb",
    "\\longleftarrow" to "\\dotsb",
    "\\Longleftarrow" to "\\dotsb",
    "\\longleftrightarrow" to "\\dotsb",
    "\\Longleftrightarrow" to "\\dotsb",
    "\\mapsto" to "\\dotsb",
    "\\longmapsto" to "\\dotsb",
    "\\hookrightarrow" to "\\dotsb",
    "\\doteq" to "\\dotsb",
    "\\mathbin" to "\\dotsb",
    "\\mathrel" to "\\dotsb",
    "\\relbar" to "\\dotsb",
    "\\Relbar" to "\\dotsb",
    "\\xrightarrow" to "\\dotsb",
    "\\xleftarrow" to "\\dotsb",
    "\\DOTSI" to "\\dotsi",
    "\\int" to "\\dotsi",
    "\\oint" to "\\dotsi",
    "\\iint" to "\\dotsi",
    "\\iiint" to "\\dotsi",
    "\\iiiint" to "\\dotsi",
    "\\idotsint" to "\\dotsi",
    "\\DOTSX" to "\\dotsx",
)

private val dotsbGroups = setOf("bin", "rel")

private val spaceAfterDots = setOf(
    ")",
    "]",
    "\\rbrack",
    "\\}",
    "\\rbrace",
    "\\rangle",
    "\\rceil",
    "\\rfloor",
    "\\rgroup",
    "\\rmoustache",
    "\\right",
    "\\bigr",
    "\\biggr",
    "\\Bigr",
    "\\Biggr",
    "\$",
    ";",
    ".",
    ",",
)

private fun isTruthy(definition: MacroDefinition?): Boolean =
    definition != null && !(definition is MacroString && definition.text.isEmpty())

private fun braketHelper(one: Boolean): (MacroContext) -> MacroDefinition = { context ->
    val left = context.consumeArg().tokens
    val middle = context.consumeArg().tokens
    val middleDouble = context.consumeArg().tokens
    val right = context.consumeArg().tokens
    val oldMiddle = context.macros.get("|")
    val oldMiddleDouble = context.macros.get("\\|")
    context.macros.beginGroup()
    val midMacro = { double: Boolean ->
        MacroFunction { inner ->
            if (one) {
                inner.macros.set("|", oldMiddle)
                if (middleDouble.isNotEmpty()) {
                    inner.macros.set("\\|", oldMiddleDouble)
                }
            }
            var doubled = double
            if (!double && middleDouble.isNotEmpty()) {
                val nextToken = inner.future()
                if (nextToken.text == "|") {
                    inner.popToken()
                    doubled = true
                }
            }
            MacroExpansion(tokens = if (doubled) middleDouble else middle, numArgs = 0)
        }
    }
    context.macros.set("|", midMacro(false))
    if (middleDouble.isNotEmpty()) {
        context.macros.set("\\|", midMacro(true))
    }
    val arg = context.consumeArg().tokens
    val expanded = context.expandTokens(right + arg + left)
    context.macros.endGroup()
    MacroExpansion(tokens = expanded.reversed(), numArgs = 0)
}
