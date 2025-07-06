package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
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
     * Specifies the application command scope for this command, where the command will be pushed to.
     *
     * This will be forced to [CommandScope.GUILD] if [BApplicationConfig.forceGuildCommands] is enabled.
     *
     * **Default:** [CommandScope.GLOBAL]
     */
    val scope: CommandScope = CommandScope.GLOBAL,

    /**
     * Represents where a command can be used.
     *
     * **Default, depending on [scope]:**
     * - [Global][CommandScope.GLOBAL] : [GlobalApplicationCommandManager.Defaults.contexts]
     * - [Guild][CommandScope.GUILD] : [GuildApplicationCommandManager.Defaults.contexts]
     *
     * This will be forced to [InteractionContextType.GUILD] if [BApplicationConfig.forceGuildCommands] is enabled.
     *
     * @see InteractionContextType
     * @see TopLevelApplicationCommandBuilder.contexts DSL equivalent
     */
    val contexts: Array<InteractionContextType> = [],

    /**
     * The integration types in which this command can be installed in.
     *
     * **Default, depending on [scope]:**
     * - [Global][CommandScope.GLOBAL] : [GlobalApplicationCommandManager.Defaults.integrationTypes]
     * - [Guild][CommandScope.GUILD] : [GuildApplicationCommandManager.Defaults.integrationTypes]
     *
     * @see IntegrationType
     * @see TopLevelApplicationCommandBuilder.integrationTypes DSL equivalent
     */
    val integrationTypes: Array<IntegrationType> = [],

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** You cannot use this with [@UserPermissions][UserPermissions].
     *
     * For example, this may let administrators configure which members/roles have access to the ban command,
     * without requiring the [Ban Members][Permission.BAN_MEMBERS] permission.
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see TopLevelApplicationCommandBuilder.isDefaultLocked DSL equivalent
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
     * @see TopLevelApplicationCommandBuilder.nsfw DSL equivalent
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
     * @see SlashCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)
