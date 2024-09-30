@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

@BService
@RequiresTextCommands
internal class TextCommandsContextImpl internal constructor(
    private val serviceContainer: ServiceContainer,
    override val textConfig: BTextConfig,
) : TextCommandsContext {
    private val textPrefixSupplier: TextPrefixSupplier? by lazy { serviceContainer.getServiceOrNull() }
    override val helpBuilderConsumer: HelpBuilderConsumer? by lazy { serviceContainer.getServiceOrNull() }
    override val defaultEmbedSupplier: DefaultEmbedSupplier by lazy { serviceContainer.getService() }
    override val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier by lazy { serviceContainer.getService() }

    private val textCommandMap: MutableMap<String, TopLevelTextCommandInfoImpl> = hashMapOf()

    override val rootCommands: Collection<TopLevelTextCommandInfoImpl>
        get() = textCommandMap.values.unmodifiableView()

    private val mentionAsPrefix: String by lazy {
        // space is part of the prefix,
        // technically fixes built-in help content,
        // doesn't change anything for parsing
        serviceContainer.getService<JDA>().selfUser.asMention + " "
    }

    override fun getDefaultPrefixes(): List<String> = when {
        textConfig.usePingAsPrefix -> textConfig.prefixes + mentionAsPrefix
        else -> textConfig.prefixes
    }

    override fun getEffectivePrefixes(channel: GuildMessageChannel): List<String> {
        if (textPrefixSupplier != null) {
            return textPrefixSupplier!!.getPrefixes(channel)
        }

        val prefixes = textConfig.prefixes
        return when (textConfig.usePingAsPrefix) {
            false -> prefixes
            true -> prefixes + mentionAsPrefix
        }
    }

    override fun getPreferredPrefix(channel: GuildMessageChannel): String? {
        if (textPrefixSupplier != null) {
            return textPrefixSupplier!!.getPreferredPrefix(channel)
        }

        val prefixes = textConfig.prefixes
        return when (textConfig.usePingAsPrefix) {
            true -> mentionAsPrefix
            false -> prefixes.firstOrNull()
        }
    }

    internal fun addTextCommand(commandInfo: TopLevelTextCommandInfoImpl) {
        (commandInfo.aliases + commandInfo.name).forEach { name ->
            textCommandMap.putIfAbsentOrThrow(name, commandInfo) {
                """
                    Text command with path '${commandInfo.path}' already exists
                    First command: ${it.declarationSite}
                    Second command: ${commandInfo.declarationSite}
                """.trimIndent()
            }
        }
    }

    override fun findTextCommand(words: List<String>): TextCommandInfoImpl? {
        lateinit var info: TextCommandInfoImpl
        var map: Map<String, TextCommandInfoImpl> = textCommandMap
        for (word in words) {
            info = map[word] ?: return null
            map = info.subcommands
        }

        return info
    }

    override fun findTextSubcommands(words: List<String>): Collection<TextCommandInfoImpl> {
        val command = findTextCommand(words) ?: return emptyList()
        return command.subcommands.values
    }
}