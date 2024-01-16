package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Additional annotation for top-level slash commands.
 *
 * This is only used to specify properties on the top-level command of the annotated slash command,
 * such as the scope or top-level description.
 *
 * This can be specified at most once per top-level slash command,
 * e.g., if you have `/tag create` and `/tag edit`, you can annotate at most one of them.
 *
 * @see JDASlashCommand @JDASlashCommand
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TopLevelSlashCommandData(
    /**
     * Specifies the application command scope for this command.
     *
     * **Default:** [CommandScope.GLOBAL_NO_DM]
     */
    val scope: CommandScope = CommandScope.GLOBAL_NO_DM,

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** You cannot use this with [UserPermissions].
     *
     * For example, this may let administrators configure which members/roles have access to the ban command,
     * without requiring the [Ban Members][Permission.BAN_MEMBERS] permission.
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see TopLevelSlashCommandBuilder.isDefaultLocked DSL equivalent
     */
    val defaultLocked: Boolean = false,

    /**
     * Specifies whether the application command is usable in NSFW channels.
     *
     * Note: NSFW commands need to be enabled by the user to appear in DMs.
     *
     * See the [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007) for more details.
     *
     * **Default:** false
     *
     * @return `true` if the command is restricted to NSFW channels
     *
     * @see TopLevelSlashCommandBuilder.nsfw DSL equivalent
     */
    val nsfw: Boolean = false,

    /**
     * Short description of the command displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped, example: `ban.description`.
     *
     * **Note:** A description cannot be set here and on [@JDASlashCommand][JDASlashCommand] at the same time.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)
