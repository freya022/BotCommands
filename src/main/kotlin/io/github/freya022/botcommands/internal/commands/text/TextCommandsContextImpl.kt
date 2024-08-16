package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.SettingsProvider
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

@BService
@RequiresTextCommands
internal class TextCommandsContextImpl internal constructor(
    override val textConfig: BTextConfig,
    private val settingsProvider: SettingsProvider?,
    private val textPrefixSupplier: TextPrefixSupplier?,
    override val defaultEmbedSupplier: DefaultEmbedSupplier,
    override val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier,
    override val helpBuilderConsumer: HelpBuilderConsumer?
) : TextCommandsContext {
    private val textCommandMap: MutableMap<String, TopLevelTextCommandInfoImpl> = hashMapOf()

    override val rootCommands: Collection<TopLevelTextCommandInfoImpl>
        get() = textCommandMap.values.unmodifiableView()

    override fun getEffectivePrefixes(channel: GuildMessageChannel): List<String> {
        if (textPrefixSupplier != null) {
            return textPrefixSupplier.getPrefixes(channel)
        }

        if (settingsProvider != null) {
            val prefixes = settingsProvider.getPrefixes(channel.guild)
            if (!prefixes.isNullOrEmpty()) return prefixes
        }

        val prefixes = textConfig.prefixes
        return when (textConfig.usePingAsPrefix) {
            false -> prefixes
            true -> prefixes + channel.jda.selfUser.asMention
        }
    }

    override fun getPreferredPrefix(channel: GuildMessageChannel): String? {
        if (textPrefixSupplier != null) {
            return textPrefixSupplier.getPreferredPrefix(channel)
        }

        val prefixes = textConfig.prefixes
        return when (textConfig.usePingAsPrefix) {
            true -> channel.jda.selfUser.asMention
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