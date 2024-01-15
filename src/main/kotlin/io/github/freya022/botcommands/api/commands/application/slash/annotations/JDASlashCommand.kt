package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a slash command,
 * additional properties can be set with [@TopLevelSlashCommandData][TopLevelSlashCommandData]
 * and [@SlashCommandGroupData][SlashCommandGroupData].
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups)
 * on which paths are allowed.
 *
 * ### Additional annotations
 * Additional data can be set once **per top-level name** with [@TopLevelSlashCommandData][TopLevelSlashCommandData],
 * and once **per subcommand group** with [@SlashCommandGroupData][SlashCommandGroupData].
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command] and extend [ApplicationCommand].
 * - First parameter must be [GlobalSlashEvent] for [global][CommandScope.GLOBAL] commands, or,
 * [GuildSlashEvent] for [global guild-only][CommandScope.GLOBAL_NO_DM] and [guild][CommandScope.GUILD] commands.
 *
 * ### Option types
 * - Input options: Uses [@SlashOption][SlashOption], supported types are in [ParameterResolver],
 * additional types can be added by implementing [SlashParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options and services: No annotation, additional types can be added by implementing [ICustomResolver].
 *
 * @see Command @Command
 * @see TopLevelSlashCommandData @TopLevelSlashCommandData
 * @see SlashCommandGroupData @SlashCommandGroupData
 * @see SlashOption @SlashOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Filter @Filter
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager.slashCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDASlashCommand(
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
     * **Note:** A description cannot be set here and on [@TopLevelSlashCommandData][TopLevelSlashCommandData] at the same time.
     *
     * @see LocalizationFunction
     *
     * @see SlashCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)
