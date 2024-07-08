package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.Usability
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.config.BConfig
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.function.Consumer

/**
 * Represents a text command.
 */
interface TextCommandInfo : CommandInfo {
    /**
     * Subcommands of this text command, the key is the name of the subcommand.
     */
    val subcommands: Map<String, TextCommandInfo>

    /**
     * Variations of this command, see [TextCommandBuilder.variation] for details.
     *
     * @see TextCommandBuilder.variation
     */
    val variations: List<TextCommandVariation>

    /**
     * Aliases for this text command.
     *
     * Allows using this command/subcommand with a different name.
     */
    val aliases: List<String>

    /**
     * General description of this text command, part of the built-in help command.
     *
     * This is different from the [variation's description][TextCommandVariation.description],
     * as this one displays before any variation is displayed.
     */
    val description: String?

    /**
     * Whether this command should only be executable in [NSFW channels][IAgeRestrictedChannel].
     */
    val nsfw: Boolean

    /**
     * Whether this command can only be run by the [bot owners][BConfig.ownerIds].
     *
     * Owner-only commands are hidden in the built-in help content,
     * but will still be responded to if a user tries to use it,
     * though they will be rejected.
     */
    val isOwnerRequired: Boolean

    /**
     * Whether this command is hidden.
     *
     * This command and its subcommands are hidden from help content and cannot run,
     * except for [bot owners][BConfig.ownerIds].
     */
    val hidden: Boolean

    /**
     * Consumer of an [EmbedBuilder], runs after generating the built-in help content.
     */
    val detailedDescription: Consumer<EmbedBuilder>?

    /**
     * Returns a [Usability] instance, representing whether this text command can be used,
     * and if it is visible, for example, in the help content.
     */
    fun getUsability(member: Member, channel: GuildMessageChannel): Usability
}