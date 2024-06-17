package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.regex.Pattern
import kotlin.reflect.KVisibility
import kotlin.reflect.full.staticProperties

private val logger = KotlinLogging.logger { }

internal object CommandPattern {
    fun of(variation: TextCommandVariationImpl): Regex {
        val optionParameters: List<TextCommandOptionImpl> = variation.parameters
            .flatMap { it.allOptions }
            .filterIsInstance<TextCommandOptionImpl>()
        val hasMultipleQuotable = variation.hasMultipleQuotable

        val patterns = optionParameters.map { ParameterPattern(it.resolver, it.isOptionalOrNullable, hasMultipleQuotable) }
        val pattern = joinPatterns(patterns)

        //Try to match the built pattern to a built example string,
        // if this fails then the pattern (and the command) is deemed too complex to be used
        val exampleStr = optionParameters.filter { it.isRequired }.joinToString(" ") { it.resolver.testExample }
        require(pattern.matches(exampleStr)) {
            """
                Failed building text command pattern, you can try to either rearrange the arguments as to make a parse-able command, especially moving parameters which are parsed from strings, or, use slash commands 
                Text command at: ${variation.function.shortSignature} 
                Pattern: $pattern
                Test string: $exampleStr
            """.trimIndent()
        }

        return pattern
    }

    enum class SpacePosition {
        LEFT, RIGHT
    }

    class ParameterPattern(
        resolver: TextParameterResolver<*, *>,
        val optional: Boolean,
        hasMultipleQuotable: Boolean
    ) {
        private val pattern: Pattern = when {
            hasMultipleQuotable -> resolver.preferredPattern //Might be a quotable pattern
            else -> resolver.pattern
        }

        private val flagExpressions: String?

        init {
            require(pattern.matcher("").groupCount() > 0) {
                // Signature is not available here as resolver might be framework-provided (and so metadata not read)
                "Regex patterns of ${resolver.javaClass.simpleNestedName} must have at least 1 capturing group"
            }

            val flags: List<String> = buildList(7) {
                var appliedFlags = 0
                fun tryAddFlag(flag: Int, flagExpression: String) {
                    if (pattern.flags() and flag == flag) {
                        appliedFlags = appliedFlags or flag
                        add(flagExpression)
                    }
                }

                tryAddFlag(Pattern.UNIX_LINES, "d")
                tryAddFlag(Pattern.CASE_INSENSITIVE, "i")
                tryAddFlag(Pattern.COMMENTS, "x")
                tryAddFlag(Pattern.MULTILINE, "m")
                tryAddFlag(Pattern.DOTALL, "s")

                if (pattern.flags() and Pattern.UNICODE_CHARACTER_CLASS == Pattern.UNICODE_CHARACTER_CLASS) {
                    appliedFlags = appliedFlags or Pattern.UNICODE_CHARACTER_CLASS
                    logger.warn { "Ignoring Pattern.UNICODE_CHARACTER_CLASS from ${resolver.javaClass.simpleNestedName} as it is already applied on the whole command pattern" }
                } else if (pattern.flags() and Pattern.UNICODE_CASE == Pattern.UNICODE_CASE) {
                    appliedFlags = appliedFlags or Pattern.UNICODE_CASE
                    logger.warn { "Ignoring Pattern.UNICODE_CASE from ${resolver.javaClass.simpleNestedName} as it is already applied on the whole command pattern" }
                }

                require(appliedFlags == pattern.flags()) {
                    // Pattern flags:         00001100
                    // Applied (known) flags: 00001000
                    // Unknown flags:         00000100

                    // Invert applied flags:  11110111
                    // And pattern flags:     00000100
                    val rawUnknownFlags = pattern.flags() and appliedFlags.inv()
                    val unknownFlags = buildList {
                        for (offset in 0..<Int.SIZE_BITS) {
                            val flag = 1 shl offset
                            if (rawUnknownFlags and flag == flag) {
                                val field = Pattern::class.staticProperties.find { it.visibility == KVisibility.PUBLIC && it.get() == flag }
                                add(field?.name ?: flag.toString())
                            }
                        }
                    }
                    "Unsupported Pattern flags in ${resolver.javaClass.simpleNestedName}: ${unknownFlags.joinToString()}"
                }
            }

            flagExpressions = when {
                flags.isEmpty() -> null
                else -> flags.joinToString("")
            }
        }

        fun toString(position: SpacePosition?): String {
            val paddedPattern = when (position) {
                SpacePosition.LEFT -> "\\s+$pattern"
                SpacePosition.RIGHT -> "$pattern\\s+"
                else -> pattern.toString()
            }
            val paddedPatternWithFlags = when {
                flagExpressions == null -> paddedPattern
                else -> "(?$flagExpressions)$paddedPattern(?-$flagExpressions)"
            }

            return when {
                optional -> "(?:$paddedPatternWithFlags)?"
                else -> paddedPatternWithFlags
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
        }.toPattern(Pattern.UNICODE_CHARACTER_CLASS).toRegex()
    }
}