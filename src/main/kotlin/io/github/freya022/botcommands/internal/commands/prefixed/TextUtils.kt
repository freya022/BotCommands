package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandBuilder.Companion.DEFAULT_DESCRIPTION
import io.github.freya022.botcommands.api.parameters.QuotableRegexParameterResolver
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

object TextUtils {
    @JvmStatic
    fun generateCommandHelp(commandInfo: TextCommandInfo, event: BaseCommandEvent): EmbedBuilder {
        val builder = event.defaultEmbed

        val name = commandInfo.path.getSpacedPath()

        val author = if (!builder.isEmpty) builder.build().author else null
        when {
            author != null -> builder.setAuthor(author.name + " – '" + name + "' command", author.url, author.iconUrl)
            else -> builder.setAuthor("${event.jda.selfUser.effectiveName} - $name", null, event.jda.selfUser.effectiveAvatarUrl)
        }

        val description = commandInfo.description
        if (description != DEFAULT_DESCRIPTION) {
            builder.appendDescription(description)
        }

        val prefix = event.context.prefix
        fun buildUsage(commandOptionsByParameters: Map<TextCommandParameter, List<TextCommandOption>>) = buildString {
            append(prefix)
            append(name)
            append(' ')

            commandOptionsByParameters.forEachUniqueOption { commandOption, hasMultipleQuotable, isOptional ->
                val boxedType = commandOption.type.jvmErasure
                val argName = getArgName(hasMultipleQuotable, commandOption, boxedType)

                append(if (isOptional) '[' else '`')
                append(argName)
                if (commandOption.isVararg) append("...")
                append(if (isOptional) ']' else '`')
                append(' ')
            }
        }

        fun buildExample(commandOptionsByParameters: Map<TextCommandParameter, List<TextCommandOption>>) = buildString {
            append(prefix)
            append(name)
            append(' ')

            commandOptionsByParameters.forEachUniqueOption { commandOption, hasMultipleQuotable, _ ->
                append(getArgExample(hasMultipleQuotable, commandOption, event))
                append(' ')
            }
        }

        if (commandInfo.variations.size == 1) {
            val commandOptionsByParameters = commandInfo.variations.single().getCommandOptionsByParameters()
            builder.appendDescription("\n**Usage:** ${buildUsage(commandOptionsByParameters)}")
        } else {
            builder.addField("Usages", buildString {
                commandInfo.variations.forEachIndexed { i, variation ->
                    val commandOptionsByParameters = variation.getCommandOptionsByParameters()
                    appendLine("${i + 1}. ${buildUsage(commandOptionsByParameters)}")
                    //TODO description
                    appendLine("  - **Example:** ${buildExample(commandOptionsByParameters)}")
                }
            }, false)
        }

        val textSubcommands = event.context.textCommandsContext.findTextSubcommands(commandInfo.path.components)
        if (textSubcommands.isNotEmpty()) {
            val subcommandHelp = textSubcommands
                .joinToString("\n - ") { subcommandInfo: TextCommandInfo ->
                    "**" + subcommandInfo.path.components.drop(commandInfo.path.nameCount).joinToString(" ") + "** : " + subcommandInfo.description
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
    fun Map<TextCommandParameter, List<TextCommandOption>>.forEachUniqueOption(block: (commandOption: TextCommandOption, hasMultipleQuotable: Boolean, isOptional: Boolean) -> Unit) {
        forEach { (parameter, commandOptions) ->
            val hasMultipleQuotable = commandOptions.hasMultipleQuotable()

            for (commandOption in commandOptions) {
                val isOptional = commandOption.isOptionalOrNullable
                block(commandOption, hasMultipleQuotable, isOptional)

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