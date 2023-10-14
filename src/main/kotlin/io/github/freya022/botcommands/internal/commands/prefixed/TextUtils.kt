package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.parameters.QuotableRegexParameterResolver
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

object TextUtils {
    private const val USAGE_MAX_LENGTH = 512
    private const val EXAMPLE_MAX_LENGTH = 1024

    @JvmStatic
    fun generateCommandHelp(commandInfo: TextCommandInfo, event: BaseCommandEvent): EmbedBuilder {
        val builder = event.defaultEmbed

        val name = commandInfo.path.getSpacedPath()

        val author = if (!builder.isEmpty) builder.build().author else null
        when {
            author != null -> builder.setAuthor(author.name + " – '" + name + "' command", author.url, author.iconUrl)
            else -> builder.setAuthor("${event.jda.selfUser.effectiveName} - $name", null, event.jda.selfUser.effectiveAvatarUrl)
        }

        commandInfo.description?.let { builder.appendDescription(it) }

        val prefix = event.context.prefix
        fun TextCommandVariation.buildUsage(commandOptionsByParameters: Map<TextCommandParameter, List<TextCommandOption>>) = buildString {
            append(prefix)
            append(name)
            append(' ')

            if (usage != null) {
                append(usage)
            } else {
                commandOptionsByParameters.forEachUniqueOption { commandOption, hasMultipleQuotable, isOptional ->
                    val boxedType = commandOption.type.jvmErasure
                    val argUsagePart = buildString {
                        append(if (isOptional) '[' else '`')
                        append(getArgName(hasMultipleQuotable, commandOption, boxedType))
                        if (commandOption.isVararg) append("...")
                        append(if (isOptional) ']' else '`')
                        append(' ')
                    }

                    if (length + argUsagePart.length + 4 /* truncated */ < USAGE_MAX_LENGTH) {
                        append(argUsagePart)
                        true
                    } else {
                        append(" ...")
                        false
                    }
                }
            }
        }

        fun TextCommandVariation.buildExample(commandOptionsByParameters: Map<TextCommandParameter, List<TextCommandOption>>) = buildString {
            append(prefix)
            append(name)
            append(' ')

            if (example != null) {
                append(example)
            } else {
                commandOptionsByParameters.forEachUniqueOption { commandOption, hasMultipleQuotable, _ ->
                    val argExample = getArgExample(hasMultipleQuotable, commandOption, event)
                    if (length + argExample.length + 1 /* space */ + 4 /* truncated */ < EXAMPLE_MAX_LENGTH) {
                        append(argExample)
                        append(' ')
                        true
                    } else {
                        append(" ...")
                        false
                    }
                }
            }
        }

        if (commandInfo.variations.size == 1) {
            val variation = commandInfo.variations.single()
            val commandOptionsByParameters = variation.getCommandOptionsByParameters()
            variation.description?.let { builder.appendDescription("\n$it") }
            builder.appendDescription("\n**Usage:** ${variation.buildUsage(commandOptionsByParameters)}")
            builder.appendDescription("\n**Example:** ${variation.buildExample(commandOptionsByParameters)}")
        } else {
            builder.appendDescription("\n### Usages:\n")
            builder.appendDescription(commandInfo.variations.mapIndexed { i, variation ->
                buildString {
                    val commandOptionsByParameters = variation.getCommandOptionsByParameters()
                    appendLine("${i + 1}. ${variation.buildUsage(commandOptionsByParameters)}")
                    variation.description?.let { appendLine("  - $it") }
                    append("  - **Example:** ${variation.buildExample(commandOptionsByParameters)}")
                }
            }.joinToString(separator = "\n"))
        }

        val textSubcommands = event.context.textCommandsContext.findTextSubcommands(commandInfo.path.components)
        if (textSubcommands.isNotEmpty()) {
            val subcommandHelp = textSubcommands
                .joinToString("\n - ") { subcommandInfo: TextCommandInfo ->
                    "**" + subcommandInfo.path.components.drop(commandInfo.path.nameCount).joinToString(" ") + "** : " + (subcommandInfo.description ?: "No description")
                }

            builder.addField("Subcommands", subcommandHelp, false)
        }

        commandInfo.detailedDescription?.accept(builder)

        return builder
    }

    private fun getArgExample(needsQuote: Boolean, commandOption: TextCommandOption, event: BaseCommandEvent): String {
        val example = commandOption.helpExample
            ?: commandOption.resolver.getHelpExample(commandOption.kParameter, event, commandOption.isId)

        return when {
            needsQuote && commandOption.resolver is QuotableRegexParameterResolver -> "\"$example\""
            else -> example
        }
    }

    private fun getArgName(needsQuote: Boolean, commandOption: TextCommandOption, clazz: KClass<*>): String {
        val argumentName = commandOption.helpName
        return when (clazz) {
            String::class -> when {
                needsQuote -> "\"" + argumentName + "\""
                else -> argumentName
            }
            else -> argumentName
        }
    }

    @JvmStatic
    fun List<TextCommandOption>.hasMultipleQuotable(): Boolean =
        count { o -> o.resolver is QuotableRegexParameterResolver } > 1

    @JvmStatic
    fun TextCommandVariation.getCommandOptionsByParameters() = buildMap(parameters.size * 2) {
        parameters.forEach {
            val allOptions = it.allOptions.filterIsInstance<TextCommandOption>()
            if (allOptions.isNotEmpty())
                this[it] = allOptions
        }
    }

    /**
     * Only runs one option from a vararg parameter
     */
    @JvmStatic
    fun Map<TextCommandParameter, List<TextCommandOption>>.forEachUniqueOption(block: (commandOption: TextCommandOption, hasMultipleQuotable: Boolean, isOptional: Boolean) -> Boolean) {
        forEach { (parameter, commandOptions) ->
            val hasMultipleQuotable = commandOptions.hasMultipleQuotable()

            for (commandOption in commandOptions) {
                val isOptional = commandOption.isOptionalOrNullable
                if (!block(commandOption, hasMultipleQuotable, isOptional)) {
                    return
                }

                // Only run on one option if the containing parameter is a vararg
                if (parameter.isVararg) break
            }
        }
    }

    @JvmStatic
    fun <T : IMentionable> findEntity(id: Long, collection: Collection<T>, valueSupplier: () -> T): T =
        collection.find { user -> user.idLong == id } ?: valueSupplier()

    @JvmSynthetic
    inline fun <T : IMentionable> Collection<T>.findEntity(id: Long, valueSupplier: () -> T): T =
        find { user -> user.idLong == id } ?: valueSupplier()

    fun CommandPath.getSpacedPath(): String = getFullPath(' ')

    val CommandPath.components: List<String>
        get() = fullPath.split(' ')
}