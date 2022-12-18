package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.hasMultipleQuotable
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import java.util.regex.Pattern

object CommandPattern {
    fun of(variation: TextCommandVariation): Pattern {
        val optionParameters: List<TextCommandParameter> = variation.optionParameters
        val hasMultipleQuotable = optionParameters.hasMultipleQuotable()

        val patterns = optionParameters.map { ParameterPattern(it.resolver, it.isOptional, hasMultipleQuotable) }
        val pattern = joinPatterns(patterns)

        //Try to match the built pattern to a built example string,
        // if this fails then the pattern (and the command) is deemed too complex to be used
        val exampleStr = optionParameters.joinToString(" ") { it.resolver.testExample }
        require(pattern.matcher(exampleStr).matches()) {
            """
            Failed building pattern for method ${variation.method.shortSignature} with pattern '$pattern' and example '$exampleStr'
            You can try to either rearrange the arguments as to make a parse-able command, especially moving parameters which are parsed from strings, or, use slash commands""".trimIndent()
        }

        return pattern
    }

    class ParameterPattern(
        resolver: RegexParameterResolver<*, *>,
        private val optional: Boolean,
        hasMultipleQuotable: Boolean
    ) {
        private val pattern: Pattern = when {
            hasMultipleQuotable -> resolver.preferredPattern //Might be a quotable pattern
            else -> resolver.pattern
        }

        fun toString(includeSpace: Boolean): String {
            return if (optional) {
                if (includeSpace) "(?:$pattern\\s+)?" else "(?:$pattern)?"
            } else {
                if (includeSpace) "$pattern\\s+" else pattern.toString()
            }
        }
    }

    @JvmStatic
    fun joinPatterns(patterns: List<ParameterPattern>): Pattern {
        return buildString(16 * patterns.size) {
            append("^")

            patterns.forEachIndexed { i, pattern ->
                val includeSpace = i <= patterns.size - 2
                append(pattern.toString(includeSpace))
            }
        }.let { Pattern.compile(it) }
    }
}