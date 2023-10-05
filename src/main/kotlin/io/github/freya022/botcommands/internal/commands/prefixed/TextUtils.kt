package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandBuilder.Companion.DEFAULT_DESCRIPTION
import io.github.freya022.botcommands.api.parameters.QuotableRegexParameterResolver
import io.github.freya022.botcommands.internal.core.BContextImpl
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
            else -> builder.setAuthor("'$name' command")
        }

        val description = commandInfo.description
        if (description != DEFAULT_DESCRIPTION) {
            builder.addField("Description", description, false)
        }

        val prefix = event.context.prefix
        for ((i, variation) in commandInfo.variations.withIndex()) {
            val commandOptions = variation.parameters.flatMap { it.allOptions }.filterIsInstance<TextCommandOption>()

            val syntax = StringBuilder("**Syntax**: $prefix$name ")
            val example = StringBuilder("**Example**: $prefix$name ")

            if (commandOptions.isNotEmpty()) {
                val needsQuote = commandOptions.hasMultipleQuotable()

                for (commandOption in commandOptions) {
                    val boxedType = commandOption.type.jvmErasure

                    val argName = getArgName(needsQuote, commandOption, boxedType)
                    val argExample = getArgExample(needsQuote, commandOption, event)

                    val isOptional = commandOption.isOptionalOrNullable
                    syntax.append(if (isOptional) '[' else '`').append(argName).append(if (isOptional) ']' else '`').append(' ')
                    example.append(argExample).append(' ')
                }
            }

            val effectiveCandidateDescription = when (description) {
                DEFAULT_DESCRIPTION -> ""
                else -> "**Description**: $description\n"
            }

            if (commandInfo.variations.size == 1) {
                builder.addField("Usage", "$effectiveCandidateDescription$syntax\n$example", false)
            } else if (commandInfo.variations.size > 1) {
                builder.addField("Overload #${i + 1}", "$effectiveCandidateDescription$syntax\n$example", false)
            }
        }

        val textSubcommands = (event.context as BContextImpl).textCommandsContext.findTextSubcommands(commandInfo.path.components)
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
        count { p -> p.resolver is QuotableRegexParameterResolver } > 1

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