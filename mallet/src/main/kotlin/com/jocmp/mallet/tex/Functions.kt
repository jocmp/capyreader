package com.jocmp.mallet.tex

import com.jocmp.mallet.tex.environments.defineArrayEnvironments
import com.jocmp.mallet.tex.environments.defineCdEnvironment
import com.jocmp.mallet.tex.functions.*

enum class ArgType {
    COLOR,
    SIZE,
    URL,
    RAW,
    ORIGINAL,
    HBOX,
    PRIMITIVE,
    MATH,
    TEXT,
}

class FunctionContext(
    val funcName: String,
    val parser: Parser,
    val token: Token? = null,
    val breakOnTokenText: String? = null,
)

typealias FunctionHandler = (context: FunctionContext, args: List<ParseNode>, optArgs: List<ParseNode?>) -> ParseNode

typealias HtmlBuilder = (group: ParseNode, options: Options) -> HtmlDomNode

interface ArgSpec {
    val type: String
    val numArgs: Int
    val argTypes: List<ArgType?>?
    val numOptionalArgs: Int
}

class FunctionSpec(
    override val type: String,
    override val numArgs: Int,
    override val argTypes: List<ArgType?>? = null,
    val allowedInArgument: Boolean = false,
    val allowedInText: Boolean = false,
    val allowedInMath: Boolean = true,
    override val numOptionalArgs: Int = 0,
    val infix: Boolean = false,
    val primitive: Boolean = false,
    val handler: FunctionHandler?,
) : ArgSpec

class EnvContext(
    val mode: Mode,
    val envName: String,
    val parser: Parser,
)

typealias EnvHandler = (context: EnvContext, args: List<ParseNode>, optArgs: List<ParseNode?>) -> ParseNode

class EnvSpec(
    override val type: String,
    override val numArgs: Int,
    override val argTypes: List<ArgType?>? = null,
    val allowedInText: Boolean = false,
    override val numOptionalArgs: Int = 0,
    val handler: EnvHandler,
) : ArgSpec

/** KaTeX's defineFunction/defineEnvironment registries. */
object Functions {
    val functions = HashMap<String, FunctionSpec>()
    val environments = HashMap<String, EnvSpec>()
    val htmlGroupBuilders = HashMap<String, HtmlBuilder>()

    init {
        defineAccent()
        defineAccentUnder()
        defineArrow()
        definePmb()
        defineCdEnvironment()
        defineChar()
        defineColor()
        defineCr()
        defineDef()
        defineDelimSizing()
        defineEnclose()
        defineEnvironmentFunction()
        defineFont()
        defineGenFrac()
        defineHorizBrace()
        defineHref()
        defineHBox()
        defineHtml()
        defineHtmlMathml()
        defineIncludeGraphics()
        defineKern()
        defineLap()
        defineMath()
        defineMathChoice()
        defineMClass()
        defineOp()
        defineOperatorName()
        defineOrdGroup()
        defineOverline()
        definePhantom()
        defineRaiseBox()
        defineReflectBox()
        defineRelax()
        defineRule()
        defineSizing()
        defineSmash()
        defineSqrt()
        defineStyling()
        defineSupSub()
        defineSymbolsOp()
        defineSymbolsOrd()
        defineSymbolsSpacing()
        defineTag()
        defineText()
        defineUnderline()
        defineVCenter()
        defineVerb()
        defineArrayEnvironments()
    }
}

fun defineFunction(
    type: String,
    names: List<String>,
    numArgs: Int = 0,
    argTypes: List<ArgType?>? = null,
    allowedInArgument: Boolean = false,
    allowedInText: Boolean = false,
    allowedInMath: Boolean = true,
    numOptionalArgs: Int = 0,
    infix: Boolean = false,
    primitive: Boolean = false,
    handler: FunctionHandler?,
    htmlBuilder: HtmlBuilder? = null,
) {
    val spec =
        FunctionSpec(
            type = type,
            numArgs = numArgs,
            argTypes = argTypes,
            allowedInArgument = allowedInArgument,
            allowedInText = allowedInText,
            allowedInMath = allowedInMath,
            numOptionalArgs = numOptionalArgs,
            infix = infix,
            primitive = primitive,
            handler = handler,
        )
    names.forEach { Functions.functions[it] = spec }
    if (htmlBuilder != null) {
        Functions.htmlGroupBuilders[type] = htmlBuilder
    }
}

fun defineFunctionBuilders(type: String, htmlBuilder: HtmlBuilder) {
    Functions.htmlGroupBuilders[type] = htmlBuilder
}

fun defineEnvironment(
    type: String,
    names: List<String>,
    numArgs: Int = 0,
    handler: EnvHandler,
    htmlBuilder: HtmlBuilder? = null,
) {
    val spec = EnvSpec(type = type, numArgs = numArgs, handler = handler)
    names.forEach { Functions.environments[it] = spec }
    if (htmlBuilder != null) {
        Functions.htmlGroupBuilders[type] = htmlBuilder
    }
}
