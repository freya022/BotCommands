package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import net.dv8tion.jda.internal.utils.Checks

/**
 * Additional annotation for text commands.
 *
 * This is only used to specify properties on the annotated text command.
 *
 * This can be specified at most once per slash command path,
 * e.g., if you have `tag create` and `tag edit`, you can annotate at most one of them.
 *
 * @see JDATextCommandVariation @JDATextCommandVariation
 *
 * @see BotPermissions @BotPermissions
 * @see UserPermissions @UserPermissions
 * @see RateLimit @RateLimit
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextCommandData(
    /**
     * Path components of the command this annotation applies to,
     * limited to three components and composed of [`a-zA-Z1-9_-`][Checks.ALPHANUMERIC_WITH_DASH]
     *
     * If the path is omitted or empty, then the path of the [variation][JDATextCommandVariation] is used.
     */
    val path: Array<out String> = [],
    /**
     * Alternative names for this command's path fragment, **must not contain any spaces**,
     * and must follow the same format as slash commands such as `name group subcommand`
     *
     * @see TextCommandBuilder.aliases DSL equivalent
     */
    val aliases: Array<String> = [],

    /**
     * Short description of the command, displayed in the description of the built-in help command.
     *
     * @see TextCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)