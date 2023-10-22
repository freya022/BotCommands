package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a slash command.
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups)
 * on which paths are allowed.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command] and extend [ApplicationCommand].
 * - First parameter must be [GlobalSlashEvent] for [global][CommandScope.GLOBAL] commands, or,
 * [GuildSlashEvent] for [global guild-only][CommandScope.GLOBAL_NO_DM] and [guild][CommandScope.GUILD] commands.
 *
 * ### Option types
 * - Input options: Uses [@SlashOption][SlashOption], see supported types at [ParameterResolver],
 * additional resolvers can be implemented with [SlashParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options and services: No annotation, additional resolvers can be implemented with [ICustomResolver].
 *
 * @see Command @Command
 * @see SlashOption @SlashOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager.slashCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDASlashCommand(
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
     * Specifies whether the application command is usable in NSFW channels.<br>
     * Note: NSFW commands need to be enabled by the user to appear in DMs
     *
     * **Default:** false
     *
     * See the [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007) for more details.
     *
     * @return `true` if the command is restricted to NSFW channels
     *
     * @see TopLevelSlashCommandBuilder.nsfw DSL equivalent
     */
    val nsfw: Boolean = false,

    /**
     * The top-level name of the command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     */
    val name: String,

    /**
     * Command group of this command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.subcommandGroup DSL equivalent
     */
    val group: String = "",

    /**
     * Subcommand name of this command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.subcommand DSL equivalent (top-level command)
     * @see SlashSubcommandGroupBuilder.subcommand DSL equivalent (subcommand)
     */
    val subcommand: String = "",

    /**
     * Short description of the command displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped, example: `ban.description`.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)
