package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

/**
 * Supplies the prefixes which the bot would respond to in a specific guild.
 *
 * **Usage**: Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see getPrefixes
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
interface TextPrefixSupplier {
    /**
     * Returns the prefixes this bot responds to in the specified guild,
     * or an empty list if the bot shouldn't respond to anything.
     *
     * The prefixes returned will be the only ones checked for when receiving a text command,
     * if you want it to reply to [configured prefixes][BTextConfig.prefixes] and/or [ping-as-prefix][BTextConfig.usePingAsPrefix],
     * you'll need to pass back those ([BTextConfig.prefixes] + [JDA.getSelfUser().asMention][User.getAsMention]).
     *
     * Returning an empty list means the bot will not respond to commands in that guild.
     *
     * @param guild The guild in which the command is executed
     */
    fun getPrefixes(guild: Guild): List<String>

    /**
     * Returns the preferred prefix this bot is able to respond to, in the provided guild.
     *
     * If you have no preferred prefix for this guild,
     * you can return one of the [prefixes][BTextConfig.prefixes],
     * or [JDA.getSelfUser().asMention][User.getAsMention]).
     *
     * Used by the built-in help command to show command examples.
     *
     * @param guild The guild in which the command would be executed
     */
    fun getPreferredPrefix(guild: Guild): String
}