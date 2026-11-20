package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeGlue

internal fun defineKern() {
    defineFunction(
        type = "kern",
        names = listOf("\\kern", "\\mkern", "\\hskip", "\\mskip"),
        numArgs = 1,
        argTypes = listOf(ArgType.SIZE),
        primitive = true,
        allowedInText = true,
        handler = { context, args, _ ->
            val parser = context.parser
            val funcName = context.funcName
            val size = assertNodeType<ParseNode.Size>(args[0])
            val mathFunction = funcName[1] == 'm'
            val muUnit = size.value.unit == "mu"
            if (mathFunction) {
                if (!muUnit) {
                    parser.settings.reportNonstrict(
                        "mathVsTextUnits",
                        "LaTeX's $funcName supports only mu units, not ${size.value.unit} units",
                    )
                }
                if (parser.mode != Mode.MATH) {
                    parser.settings.reportNonstrict("mathVsTextUnits", "LaTeX's $funcName works only in math mode")
                }
            } else {
                if (muUnit) {
                    parser.settings.reportNonstrict("mathVsTextUnits", "LaTeX's $funcName doesn't support mu units")
                }
            }
            ParseNode.Kern(
                mode = parser.mode,
                dimension = size.value,
            )
        },
        htmlBuilder = { group, options ->
            makeGlue(assertNodeType<ParseNode.Kern>(group).dimension, options)
        },
    )
}
