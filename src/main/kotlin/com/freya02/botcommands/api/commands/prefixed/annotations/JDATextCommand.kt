package com.freya02.botcommands.api.commands.prefixed.annotations

import com.freya02.botcommands.api.commands.annotations.BotPermissions
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.api.core.options.annotations.Aggregate
import com.freya02.botcommands.api.parameters.ParameterResolver

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
 * Input options need to be annotated with [@TextOption][TextOption], see supported types at [ParameterResolver].
 *
 * @see Command @Command
 * @see TextOption @TextOption
 * @see Hidden @Hidden
 * @see ID @ID
 * @see BotPermissions @BotPermissions
 * @see UserPermissions @UserPermissions
 * @see Cooldown @Cooldown
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
     * Primary name of the command, **must not contain any spaces**
     *
     * @see JDATextCommand.name DSL equivalent
     */
    val name: String,

    /**
     * Group name of the command, **must not contain any spaces**
     *
     * @see JDATextCommand.group DSL equivalent
     */
    val group: String = "",

    /**
     * Subcommand name of the command, **must not contain any spaces**
     *
     * @see JDATextCommand.subcommand DSL equivalent
     */
    val subcommand: String = "",

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
     * Short description of the command displayed in the help command
     *
     * @see TextCommandBuilder.description DSL equivalent
     */
    val description: String = TextCommandBuilder.DEFAULT_DESCRIPTION
)
