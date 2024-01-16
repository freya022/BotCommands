package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.internal.utils.Checks

/**
 * Declares this function as a text command,
 * additional properties can be set with [@TextCommandData][TextCommandData].
 *
 * ### Additional annotations
 * Additional data can be set by using [@TextCommandData][TextCommandData] once **per top-level name**,
 * by specifying their [target path][TextCommandData.path].
 *
 * ### Text command variations
 * A given text command path (such as `ban temp`) is composed of at least one variation;
 * Each variation has different parameters, and will display separately in the built-in help content.
 *
 * Each variation runs based off its [priority][order],
 * the first variation that has a full match against the user input gets executed.
 *
 * If no regex-based variation (using a [BaseCommandEvent]) matches,
 * the fallback variation is executed (if a variation using [CommandEvent] exists).
 *
 * If no variation matches and there is no fallback,
 * then the [help content][IHelpCommand.onInvalidCommand] is invoked for the command.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command] and extend [TextCommand]
 * - First parameter must be [BaseCommandEvent], or, [CommandEvent] for fallback commands/manual token consumption.
 *
 * ### Option types
 * - Input options: Uses [@TextOption][TextOption], supported types are in [ParameterResolver],
 * additional types can be added by implementing [TextParameterResolver].
 * - [TextLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options and services: No annotation, additional types can be added by implementing [ICustomResolver].
 *
 * @see Command @Command
 * @see TextCommandData @TextCommandData
 * @see Category @Category
 * @see TextOption @TextOption
 * @see Hidden @Hidden
 * @see NSFW @NSFW
 * @see BotPermissions @BotPermissions
 * @see UserPermissions @UserPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Filter @Filter
 *
 * @see TextDeclaration Declaring text commands using the DSL
 *
 * @see TextCommandManager.textCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDATextCommandVariation(
    /**
     * Path components of the command,
     * limited to three components and composed of [`a-zA-Z1-9_-`][Checks.ALPHANUMERIC_WITH_DASH]
     */
    val path: Array<out String>,

    /**
     * Specifies the priority of this text command variation (1 is the most important).
     *
     * By default, if two variations have no order set, parameters are compared between each variation,
     * if one of them is a [String] but the other is not, the [String] parameter is prioritized.
     *
     * If you are using Kotlin, [DSL-declared commands][TextDeclaration] retain the order they are declared in.
     */
    val order: Int = 0,

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
