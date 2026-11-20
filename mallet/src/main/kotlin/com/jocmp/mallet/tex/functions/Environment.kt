package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.EnvContext
import com.jocmp.mallet.tex.Functions
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.assertCharacterGroup
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction

internal fun defineEnvironmentFunction() {
    defineFunction(
        type = "environment",
        names = listOf("\\begin", "\\end"),
        numArgs = 1,
        argTypes = listOf(ArgType.TEXT),
        handler = handler@{ context, args, _ ->
            val parser = context.parser
            val nameGroup = args[0]
            if (nameGroup !is ParseNode.OrdGroup) {
                throw ParseError("Invalid environment name", nameGroup)
            }
            val envName =
                assertCharacterGroup(
                    nameGroup,
                    "Environment name should contain only text characters and spaces",
                    true,
                )

            if (context.funcName == "\\begin") {
                val env =
                    Functions.environments[envName]
                        ?: throw ParseError("No such environment: $envName", nameGroup)
                val (envArgs, optArgs) = parser.parseArguments("\\begin{$envName}", env)
                val envContext =
                    EnvContext(
                        mode = parser.mode,
                        envName = envName,
                        parser = parser,
                    )
                val result = env.handler(envContext, envArgs, optArgs)
                parser.expect("\\end", false)
                val endNameToken = parser.fetch()
                val end = assertNodeType<ParseNode.Environment>(parser.parseFunction())
                if (end.name != envName) {
                    throw ParseError(
                        "Mismatch: \\begin{$envName} matched by \\end{${end.name}}",
                        endNameToken,
                    )
                }
                return@handler result
            }

            ParseNode.Environment(
                mode = parser.mode,
                name = envName,
                nameGroup = nameGroup,
            )
        },
    )
}
