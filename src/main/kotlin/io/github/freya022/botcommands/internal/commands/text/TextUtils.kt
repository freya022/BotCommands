package io.github.freya022.botcommands.internal.commands.text

import dev.minn.jda.ktx.messages.InlineEmbed
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

object TextUtils {
    private const val USAGE_MAX_LENGTH = 512
    private const val EXAMPLE_MAX_LENGTH = 1024

    @JvmStatic
    fun generateCommandHelp(commandInfo: TextCommandInfo, event: BaseCommandEvent): EmbedBuilder = InlineEmbed(event.defaultEmbed).apply {
        val spacedPath = commandInfo.path.getSpacedPath()

        val author = builder.takeUnless { builder.isEmpty }
            ?.build()
            ?.author
        if (author != null) {
            author {
                name = "${author.name} – '$spacedPath' command"
                url = author.url
                iconUrl = author.iconUrl
            }
        } else {
            author(name = "${event.jda.selfUser.effectiveName} - $spacedPath", iconUrl = event.jda.selfUser.effectiveAvatarUrl)
        }

        description = description.orEmpty() + generateDescription(commandInfo, event)

        val textSubcommands = commandInfo.subcommands.values
        if (textSubcommands.isNotEmpty()) {
            field(name = "Subcommands", inline = false) {
                value = buildString {
                    addSubcommands(textSubcommands)
                }
            }
        }

        commandInfo.detailedDescription?.accept(builder)
    }.builder

    private fun StringBuilder.addSubcommands(textSubcommands: Collection<TextCommandInfo>, depth: Int = 1) {
        textSubcommands.forEach { subcommandInfo ->
            val pathComponent = subcommandInfo.path.getSpacedPath()
            append("**$pathComponent**")
            subcommandInfo.description?.let { append(": $it") }
            appendLine()

            addSubcommands(subcommandInfo.subcommands.values, depth + 1)
        }
    }

    private fun generateDescription(commandInfo: TextCommandInfo, event: BaseCommandEvent) = buildString {
        val name = commandInfo.path.getSpacedPath()

        commandInfo.description?.let { appendLine(it) }

        fun StringBuilder.tryAppendSpaced(text: String, limit: Int): Boolean {
            return if (length + text.length + 4 /* truncated */ < limit) {
                append(text).append(' ')
                true
            } else {
                append(" ...")
                false
            }
        }

        val prefix = event.context.textCommandsContext.getPreferredPrefix(event.guildChannel) ?: throwInternal("Cannot generate help content without a prefix")
        fun TextCommandVariation.buildUsage(commandOptionsByParameters: Map<TextCommandParameter, List<TextCommandOption>>) = buildString {
            append(prefix)
            append(name)
            append(' ')

            if (usage != null) {
                append(usage)
            } else {
                commandOptionsByParameters.forEachUniqueOption { option ->
                    val boxedType = option.type.jvmErasure
                    val argUsagePart = buildString {
                        append(if (option.isRequired) '`' else '[')
                        append(getArgName(hasMultipleQuotable, option, boxedType))
                        if (option.isVararg) append("...")
                        append(if (option.isRequired) '`' else ']')
                    }

                    tryAppendSpaced(argUsagePart, USAGE_MAX_LENGTH)
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
                commandOptionsByParameters.forEachUniqueOption { commandOption ->
                    val argExample = getArgExample(hasMultipleQuotable, commandOption, event)
                    tryAppendSpaced(argExample, EXAMPLE_MAX_LENGTH)
                }
            }
        }

        if (commandInfo.variations.size == 1) {
            val variation = commandInfo.variations.single()
            val commandOptionsByParameters = variation.getCommandOptionsByParameters()
            variation.description?.let { appendLine().appendLine(it) }
            appendLine("**Usage:** ${variation.buildUsage(commandOptionsByParameters)}")
            appendLine("**Example:** ${variation.buildExample(commandOptionsByParameters)}")
        } else if (commandInfo.variations.isNotEmpty()) {
            appendLine("### Usages:")
            commandInfo.variations.forEachIndexed { i, variation ->
                val commandOptionsByParameters = variation.getCommandOptionsByParameters()
                appendLine("${i + 1}. ${variation.buildUsage(commandOptionsByParameters)}")
                variation.description?.let { appendLine("  - $it") }
                appendLine("  - **Example:** ${variation.buildExample(commandOptionsByParameters)}")
            }
        }
    }

    private fun getArgExample(needsQuote: Boolean, commandOption: TextCommandOption, event: BaseCommandEvent): String {
        val example = commandOption.helpExample
            ?: commandOption.getResolverHelpExample(event)

        return when {
            needsQuote && commandOption.isQuotable -> "\"$example\""
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
    fun Map<TextCommandParameter, List<TextCommandOption>>.forEachUniqueOption(block: (option: TextCommandOption) -> Boolean) {
        forEach { (parameter, commandOptions) ->
            for (commandOption in commandOptions) {
                if (!block(commandOption)) {
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