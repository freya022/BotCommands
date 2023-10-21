package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.CommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.RegexParameterResolver
import net.dv8tion.jda.internal.utils.Checks

/**
 * Declares this function as a text command.
 *
 * Text commands are composed of "variations";
 * functions with the same path form a group of variations.<br>
 * Each variation is run based off its [priority][order],
 * the first variation that has its syntax match against the user input gets executed.
 *
 * **Requirements:**
 *  - The declaring class must be annotated with [@Command][Command]
 *  - The method must be in the [search path][BConfigBuilder.addSearchPath]
 *  - First parameter must be [BaseCommandEvent], or, [CommandEvent] for fallback commands/manual token consumption
 *
 * Input options need to be annotated with [@TextOption][TextOption], see supported types at [ParameterResolver],
 * additional resolvers can be implemented with [RegexParameterResolver].
 *
 * @see Command @Command
 * @see TextOption @TextOption
 * @see Hidden @Hidden
 * @see ID @ID
 * @see BotPermissions @BotPermissions
 * @see UserPermissions @UserPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Aggregate @Aggregate
 *
 * @see AppDeclaration Declaring text commands using the DSL
 *
 * @see TextCommandManager.textCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDATextCommand(
    /**
     * Path components of the command,
     * limited to three components and composed of [`a-zA-Z1-9_-`][Checks.ALPHANUMERIC_WITH_DASH]
     */
    val path: Array<out String>,

    /**
     * Specifies the priority of this text command variation (1 is the most important)
     *
     * @see JDATextCommand.order DSL equivalent
     */
    val order: Int = 0,

    /**
     * Secondary **paths** of the command, **must not contain any spaces**,
     * and must follow the same format as slash commands such as `name group subcommand`
     *
     * @see TextCommandBuilder.aliases DSL equivalent
     */
    val aliases: Array<String> = [],

    /**
     * Short description of the command, displayed in the description of the built-in help command.
     *
     * This description can only be set once for a given command path.
     *
     * @see TextCommandBuilder.description DSL equivalent
     */
    val generalDescription: String = "",

    /**
     * Short description of the command displayed in the built-in help command,
     * below the command usage.
     *
     * @see TextCommandVariationBuilder.description DSL equivalent
     */
    val description: String = "",

    /**
     * Usage string for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see TextCommandVariationBuilder.usage DSL equivalent
     */
    val usage: String = "",

    /**
     * Example command for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see TextCommandVariationBuilder.example DSL equivalent
     */
    val example: String = ""
)
