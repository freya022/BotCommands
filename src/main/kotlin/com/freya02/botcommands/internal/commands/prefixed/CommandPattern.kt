package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.hasMultipleQuotable
import com.freya02.botcommands.internal.core.reflection.shortSignature
import java.util.regex.Pattern

internal object CommandPattern {
    fun of(variation: TextCommandVariation): Regex {
        val optionParameters: List<TextCommandOption> = variation.parameters
            .flatMap { it.allOptions }
            .filterIsInstance<TextCommandOption>()
        val hasMultipleQuotable = optionParameters.hasMultipleQuotable()

        val patterns = optionParameters.map { ParameterPattern(it.resolver, it.isOptionalOrNullable, hasMultipleQuotable) }
        val pattern = joinPatterns(patterns)

        //Try to match the built pattern to a built example string,
        // if this fails then the pattern (and the command) is deemed too complex to be used
        val exampleStr = optionParameters.filter { !it.isOptionalOrNullable }.joinToString(" ") { it.resolver.testExample }
        require(pattern.matches(exampleStr)) {
            """
            Failed building pattern for method ${variation.function.shortSignature} with pattern '$pattern' and example '$exampleStr'
            You can try to either rearrange the arguments as to make a parse-able command, especially moving parameters which are parsed from strings, or, use slash commands""".trimIndent()
        }

        return pattern
    }

    enum class SpacePosition {
        LEFT, RIGHT
    }

    class ParameterPattern(
        resolver: RegexParameterResolver<*, *>,
        val optional: Boolean,
        hasMultipleQuotable: Boolean
    ) {
        private val pattern: Pattern = when {
            hasMultipleQuotable -> resolver.preferredPattern //Might be a quotable pattern
            else -> resolver.pattern
        }

        fun toString(position: SpacePosition?): String {
            return if (optional) {
                when (position) {
                    SpacePosition.LEFT -> "(?:\\s+$pattern)?"
                    SpacePosition.RIGHT -> "(?:$pattern\\s+)?"
                    else -> "(?:$pattern)?"
                }
            } else {
                when (position) {
                    SpacePosition.LEFT -> "\\s+$pattern"
                    SpacePosition.RIGHT -> "$pattern\\s+"
                    else -> pattern.toString()
                }
            }
        }
    }

    @JvmStatic
    fun joinPatterns(patterns: List<ParameterPattern>): Regex {
        // The space must stick to the optional part when in between, while being the nearest from the middle point
        // So if arg0 is optional, but arg1 is not, the space goes on the right part of the regex of arg0
        // If arg0 is required, but arg1 is optional, the space goes on the left part of the regex of arg1
        val positions = arrayOfNulls<SpacePosition>(patterns.size)
        for (i in 0 until patterns.size - 1) {
            val arg0 = patterns[i]
            val arg1 = patterns[i + 1]
            if (arg0.optional && !arg1.optional) {
                positions[i] = SpacePosition.RIGHT
            } else if (!arg0.optional && arg1.optional) {
                positions[i + 1] = SpacePosition.LEFT
            } else if (arg0.optional /*&& arg1.optional*/) {
                positions[i + 1] = SpacePosition.LEFT
            } else { //Both are required
                positions[i + 1] = SpacePosition.LEFT
            }
        }

        return buildString(16 * patterns.size) {
            append("^")

            patterns.forEachIndexed { i, pattern ->
                val position = positions[i]
                append(pattern.toString(position))
            }
        }.let { it.toRegex() }
    }
}