package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder.Companion.defaultDescription
import com.freya02.botcommands.api.parameters.QuotableRegexParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

object TextUtils {
    fun generateCommandHelp(candidates: Collection<TextCommandInfo>, event: BaseCommandEvent): EmbedBuilder {
        val builder = event.defaultEmbed

        val commandInfo = candidates.first()
        val name = commandInfo._path.getSpacedPath()

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
        for ((i, candidate) in candidates.reversed().withIndex()) {
            val commandParameters = candidate.optionParameters

            val syntax = StringBuilder("**Syntax**: $prefix$name ")
            val example = StringBuilder("**Example**: $prefix$name ")

            if (commandParameters.isNotEmpty()) {
                val needsQuote = commandParameters.hasMultipleQuotable()

                for (commandParameter in commandParameters) {
                    val boxedType = commandParameter.type.jvmErasure

                    val argName = getArgName(needsQuote, commandParameter, boxedType)
                    val argExample = getArgExample(needsQuote, commandParameter, boxedType)

                    val isOptional = commandParameter.isOptional
                    syntax.append(if (isOptional) '[' else '`').append(argName).append(if (isOptional) ']' else '`').append(' ')
                    example.append(argExample).append(' ')
                }
            }

            val effectiveCandidateDescription = when (candidate.description) {
                defaultDescription -> ""
                else -> "**Description**: ${candidate.description}\n"
            }

            if (candidates.size == 1) {
                builder.addField("Usage", "$effectiveCandidateDescription$syntax\n$example", false)
            } else {
                builder.addField("Overload #${i + 1}", "$effectiveCandidateDescription$syntax\n$example", false)
            }
        }

        val textSubcommands = (event.context as BContextImpl).textCommandsContext.findTextSubcommands(commandInfo._path.fullPath.split('/'))
        if (textSubcommands.isNotEmpty()) {
            val subcommandHelp = textSubcommands
                .joinToString("\n - ") { subcommandInfo: TextCommandInfo ->
                    "**" + subcommandInfo._path.fullPath.split('/').drop(commandInfo._path.nameCount).joinToString(" ") + "** : " + subcommandInfo.description
                }

            builder.addField("Subcommands", subcommandHelp, false)
        }

        commandInfo.detailedDescription?.accept(builder)

        return builder
    }

    private fun getArgExample(needsQuote: Boolean, parameter: TextCommandParameter, clazz: KClass<*>): String {
        val optionalExample = parameter.data.helpExample

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
                    parameter.isId -> ThreadLocalRandom.current().nextLong(100000000000000000L, 999999999999999999L).toString()
                    else -> ThreadLocalRandom.current().nextLong(50).toString()
                }
                Float::class, Double::class -> ThreadLocalRandom.current().nextDouble(50.0).toString()
                Guild::class -> "331718482485837825"
                Role::class -> "801161492296499261"
                User::class -> "222046562543468545"
                Member::class -> "<@222046562543468545>"
                TextChannel::class -> "331718482485837825"
                else -> "?"
            }
        }
    }

    private fun getArgName(needsQuote: Boolean, commandParameter: TextCommandParameter, clazz: KClass<*>): String {
        val optionalName = commandParameter.data.helpName
        return when (clazz) {
            String::class.java -> when {
                needsQuote -> "\"" + optionalName + "\""
                else -> optionalName
            }
            else -> optionalName
        }
    }

    fun List<TextCommandParameter>.hasMultipleQuotable(): Boolean =
        count { p: TextCommandParameter -> p.resolver is QuotableRegexParameterResolver } > 1

    @JvmStatic
    fun <T : IMentionable> findEntity(id: Long, collection: Collection<T>, valueSupplier: () -> T): T =
        collection.find { user -> user.idLong == id } ?: valueSupplier()

    suspend fun <T : IMentionable> findEntitySuspend(id: Long, collection: Collection<T>, valueSupplier: suspend () -> T): T =
        collection.find { user -> user.idLong == id } ?: valueSupplier()

    fun CommandPath.getSpacedPath(): String {
        return fullPath.replace('/', ' ')
    }
}