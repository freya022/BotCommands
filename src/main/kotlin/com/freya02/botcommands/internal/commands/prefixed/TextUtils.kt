package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder.Companion.defaultDescription
import com.freya02.botcommands.api.parameters.QuotableRegexParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.util.concurrent.ThreadLocalRandom
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
        if (description != defaultDescription) {
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
                    val argExample = getArgExample(needsQuote, commandOption, boxedType)

                    val isOptional = commandOption.isOptionalOrNullable
                    syntax.append(if (isOptional) '[' else '`').append(argName).append(if (isOptional) ']' else '`').append(' ')
                    example.append(argExample).append(' ')
                }
            }

            val effectiveCandidateDescription = when (description) {
                defaultDescription -> ""
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

    private fun getArgExample(needsQuote: Boolean, commandOption: TextCommandOption, clazz: KClass<*>): String {
        val optionalExample = commandOption.helpExample

        return when {
            optionalExample != null -> when (clazz) {
                String::class -> if (needsQuote) "\"$optionalExample\"" else optionalExample
                else -> optionalExample
            }
            else -> when (clazz) {
                String::class -> if (needsQuote) "\"foo bar\"" else "foo bar"
                Emoji::class -> ":joy:"
                Int::class -> ThreadLocalRandom.current().nextLong(50).toString()
                Long::class -> when {
                    commandOption.isId -> ThreadLocalRandom.current().nextLong(100000000000000000L, 999999999999999999L).toString()
                    else -> ThreadLocalRandom.current().nextLong(50).toString()
                }
                Float::class, Double::class -> String.format(locale = null, "%.3f", ThreadLocalRandom.current().nextDouble(50.0))
                Guild::class -> "331718482485837825"
                Role::class -> "801161492296499261"
                User::class -> "222046562543468545"
                Member::class -> "<@222046562543468545>"
                TextChannel::class -> "331718482485837825"
                else -> "?"
            }
        }
    }

    private fun getArgName(needsQuote: Boolean, commandOption: TextCommandOption, clazz: KClass<*>): String {
        val optionalName = commandOption.helpName
        return when (clazz) {
            String::class -> when {
                needsQuote -> "\"" + optionalName + "\""
                else -> optionalName
            }
            else -> optionalName
        }
    }

    @JvmStatic
    fun List<TextCommandOption>.hasMultipleQuotable(): Boolean =
        count { p -> p.resolver is QuotableRegexParameterResolver } > 1

    @JvmStatic
    fun <T : IMentionable> findEntity(id: Long, collection: Collection<T>, valueSupplier: () -> T): T =
        collection.find { user -> user.idLong == id } ?: valueSupplier()

    suspend fun <T : IMentionable> findEntitySuspend(id: Long, collection: Collection<T>, valueSupplier: suspend () -> T): T =
        collection.find { user -> user.idLong == id } ?: valueSupplier()

    fun CommandPath.getSpacedPath(): String = getFullPath(' ')

    val CommandPath.components: List<String>
        get() = fullPath.split(' ')
}