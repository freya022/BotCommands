package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import java.util.regex.Pattern

object CommandPattern {
    fun of(commandInfo: TextCommandInfo): Pattern {
        val exampleBuilder = StringBuilder(commandInfo.parameters.optionCount * 16)
        val patternBuilder = StringBuilder(commandInfo.parameters.optionCount * 16)
        patternBuilder.append('^')

        val optionParameters: List<TextCommandParameter> = commandInfo.optionParameters
        val hasMultipleQuotable = Utils.hasMultipleQuotable(optionParameters)

        optionParameters.forEachIndexed { i, parameter ->
            val pattern = when {
                hasMultipleQuotable -> parameter.resolver.preferredPattern //Might be a quotable pattern
                else -> parameter.resolver.pattern
            }

            val optionalSpacePattern = if (i == 0) "" else "\\s+"
            if (parameter.isOptional) {
                patternBuilder.append("(?:").append(optionalSpacePattern).append(pattern.toString()).append(")?")
            } else {
                patternBuilder.append(optionalSpacePattern).append(pattern.toString())
                exampleBuilder.append(parameter.resolver.testExample).append(' ')
            }
        }

        val pattern = Pattern.compile(patternBuilder.toString())

        //Try to match the built pattern to a built example string,
        // if this fails then the pattern (and the command) is deemed too complex to be used
        val exampleStr = exampleBuilder.toString().trim()
        require(pattern.matcher(exampleStr).matches()) {
            """
            Failed building pattern for method ${commandInfo.method.shortSignature} with pattern '$pattern' and example '$exampleStr'
            You can try to either rearrange the arguments as to make a parsable command, especially moving parameters which are parsed from strings, or, use slash commands""".trimIndent()
        }

        return pattern
    }
}