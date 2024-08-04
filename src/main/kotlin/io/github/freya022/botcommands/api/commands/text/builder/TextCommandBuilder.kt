package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.CommandEvent
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.commands.text.annotations.NSFW
import io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.text.annotations.TextCommandData
import io.github.freya022.botcommands.api.core.BotOwners
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer
import kotlin.reflect.KFunction

interface TextCommandBuilder : CommandBuilder {
    /**
     * Marks a text command as being usable in NSFW channels only.
     *
     * ### Built-in help content
     * NSFW commands will be shown if requested in an NSFW channel.
     *
     * @see NSFW @NSFW
     */
    var nsfw: Boolean

    /**
     * Secondary **paths** of the command, **must not contain any spaces**,
     * and must follow the same format as slash commands such as `name group subcommand`
     *
     * @see TextCommandData.aliases
     */
    var aliases: MutableList<String>

    /**
     * Short description of the command, displayed in the description of the built-in help command.
     *
     * @see TextCommandData.description
     */
    var description: String?

    /**
     * Marks this text command as only usable by the bot owners.
     *
     * @see RequireOwner
     */
    var ownerRequired: Boolean

    /**
     * Hides a command and its subcommands from help content and execution,
     * except for [bot owners][BotOwners].
     *
     * @see Hidden
     */
    var hidden: Boolean

    /**
     * Returns a detailed embed of what the command is, it is used by the internal `help` command
     *
     * The `help` command will automatically set the embed title to be `Command 'command_name'` but can be overridden
     *
     * It will also set the embed's description to be the command's description, **you can override with [EmbedBuilder.setDescription]**
     *
     * @return The EmbedBuilder to use as a detailed description
     *
     * @see TextCommand.getDetailedDescription
     */
    var detailedDescription: Consumer<EmbedBuilder>?

    //TODO docs
    fun subcommand(name: String, block: TextCommandBuilder.() -> Unit)

    /**
     * Adds a variation to this text command.
     *
     * ### Text command variations
     * A given text command path (such as `ban temp`) is composed of at least one variation;
     * Each variation has different parameters, and will display separately in the built-in help content.
     *
     * Each variation runs based off insertion order,
     * the first variation that has a full match against the user input gets executed.
     *
     * If no regex-based variation (using a [BaseCommandEvent]) matches,
     * the fallback variation is executed (if a variation using [CommandEvent] exists).
     *
     * If no variation matches and there is no fallback,
     * then the [help content][IHelpCommand.onInvalidCommand] is invoked for the command.
     */
    fun variation(function: KFunction<Any>, block: TextCommandVariationBuilder.() -> Unit = {})
}
