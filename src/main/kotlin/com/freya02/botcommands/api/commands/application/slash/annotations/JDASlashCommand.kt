package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.annotations.*
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder
import com.freya02.botcommands.api.core.options.annotations.Aggregate
import com.freya02.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a slash command.
 *
 * Discord requires you to have:
 *  - 1 unique command name, examples:
 *    - `/nick`
 *
 *  - Multiple commands with the same base name but different subcommand names, examples:
 *    - `/info user`
 *    - `/info role`
 *    - `/info channel`
 *
 *  - Multiple subcommands with the same base name and base group but with different subcommand names, examples:
 *    - `/info simple user`
 *    - `/info simple role`
 *    - `/info complete user`
 *    - `/info complete role`
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups) for more details.
 *
 * Input options need to be annotated with [@SlashOption][SlashOption], see supported types at [ParameterResolver].
 *
 * **Requirement:** The declaring class must be annotated with [@Command][Command].
 *
 * @see Command @Command
 * @see SlashOption @SlashOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Aggregate @Aggregate
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
     * **Note:** you cannot use this with [UserPermissions].
     *
     * For example, maybe you want a ban command to be usable by someone who has a certain role,
     * but which doesn't have the [BAN_MEMBERS][Permission.BAN_MEMBERS] permission,
     * you would then default lock the command and let the admins of the guild configure it
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
     * Note: NSFW commands need to be enabled by the user in order to appear in DMs
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
    val description: String = SlashCommandBuilder.DEFAULT_DESCRIPTION
)
