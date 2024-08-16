package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

/**
 * Helps to get the registered text commands.
 */
@InterfacedService(acceptMultiple = false)
interface TextCommandsContext {
    val textConfig: BTextConfig

    val rootCommands: Collection<TopLevelTextCommandInfo>

    /**
     * Returns the full list of prefixes used to trigger the bot.
     *
     * This does not include ping-as-prefix.
     *
     * @return Full list of prefixes
     */
    val prefixes: List<String>
        get() = textConfig.prefixes

    /**
     * @return `true` if the bot responds to its own mention.
     */
    val isPingAsPrefix: Boolean
        get() = textConfig.usePingAsPrefix

    //TODO replace with TextCommandPrefixSupplier
    /**
     * Returns the preferred prefix for triggering this bot,
     * or `null` if [BTextConfig.usePingAsPrefix] is disabled and no prefix was added in [BTextConfig.prefixes].
     */
    fun getPreferredPrefix(jda: JDA): String? = when {
        isPingAsPrefix -> jda.selfUser.asMention + " "
        else -> prefixes.firstOrNull()
    }

    /**
     * Returns the prefixes this bot responds to in the specified guild,
     * or an empty list if the bot shouldn't respond to anything.
     *
     * As a reminder, [TextPrefixSupplier.getPrefixes] takes over the prefixes set in [BTextConfig].
     */
    fun getEffectivePrefixes(guild: Guild): List<String>

    /**
     * Returns the preferred prefix this bot is able to respond to, in the specified guild,
     * or `null` if no prefix could be determined, in which case text commands are not usable.
     *
     * As a reminder, [TextPrefixSupplier.getPreferredPrefix] takes over the prefixes set in [BTextConfig].
     */
    fun getPreferredPrefix(guild: Guild): String?

    /**
     * Returns the [DefaultEmbedSupplier] service.
     *
     * @see DefaultEmbedSupplier
     */
    val defaultEmbedSupplier: DefaultEmbedSupplier

    /**
     * Returns the [DefaultEmbedFooterIconSupplier] service.
     *
     * @see DefaultEmbedFooterIconSupplier
     */
    val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier

    /**
     * Returns the consumer that customizes the built-in help command's content.
     *
     * @see HelpBuilderConsumer
     */
    val helpBuilderConsumer: HelpBuilderConsumer?

    fun findTextCommand(words: List<String>): TextCommandInfo?

    fun findTextSubcommands(words: List<String>): Collection<TextCommandInfo>
}