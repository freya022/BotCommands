package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.NSFWStrategyBuilder
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.NSFW
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.NSFWStrategy
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.internal.utils.Checks
import java.util.function.Consumer
import kotlin.reflect.KFunction

abstract class TextCommandBuilder internal constructor(context: BContext, name: String) : CommandBuilder(context, name) {
    override val type: CommandType = CommandType.TEXT
    internal val subcommands: MutableList<TextSubcommandBuilder> = arrayListOf()

    internal val variations: MutableList<TextCommandVariationBuilder> = arrayListOf()

    internal var nsfwStrategy: NSFWStrategy? = null
        private set

    /**
     * Secondary **paths** of the command, **must not contain any spaces**,
     * and must follow the same format as slash commands such as `name group subcommand`
     *
     * @see JDATextCommand.aliases
     */
    var aliases: MutableList<String> = arrayListOf()

    /**
     * Short description of the command, displayed in the description of the built-in help command.
     *
     * @see JDATextCommand.generalDescription
     */
    var description: String? = null

    /**
     * Marks this text command as only usable by the bot owners.
     *
     * @see RequireOwner
     */
    var ownerRequired: Boolean = false

    /**
     * Hides a command from help content and execution.
     *
     * @see Hidden
     */
    var hidden: Boolean = false

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

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
    var detailedDescription: Consumer<EmbedBuilder>? = null

    fun subcommand(name: String, block: TextCommandBuilder.() -> Unit) {
        subcommands += TextSubcommandBuilder(context, name, this).apply(block)
    }

    fun variation(function: KFunction<Any>, block: TextCommandVariationBuilder.() -> Unit = {}) {
        variations += TextCommandVariationBuilder(context, function).apply(block)
    }

    /**
     * Marks a text command as being usable in NSFW channels only.
     *
     * NSFW commands will be shown in help content only if called in an NSFW channel,
     * DM consent is **not** checked as text commands are guild-only.
     *
     * @see NSFW
     */
    fun nsfw(block: NSFWStrategyBuilder.() -> Unit) {
        nsfwStrategy = NSFWStrategyBuilder().apply(block).build()
    }
}
