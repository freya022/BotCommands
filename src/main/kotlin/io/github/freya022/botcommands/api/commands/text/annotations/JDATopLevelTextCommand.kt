package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder

/**
 * Additional annotation for top-level text commands.
 *
 * This is only used to specify properties on the top-level command of the annotated text command.
 *
 * This can be used on a subcommand,
 * and specified at most once per top-level slash command,
 * e.g., if you have `tag create` and `tag edit`, you can annotate at most one of them.
 *
 * @see JDATextCommand @JDATextCommand
 *
 * @see BotPermissions @BotPermissions
 * @see UserPermissions @UserPermissions
 * @see RateLimit @RateLimit
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDATopLevelTextCommand(
    /**
     * Short description of the command, displayed in the description of the built-in help command.
     *
     * @see TextCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)