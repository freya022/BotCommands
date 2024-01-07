package io.github.freya022.botcommands.api.commands.application.context.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a user context command.
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#user-commands)
 * for more details.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command] and extend [ApplicationCommand].
 * - First parameter must be [GlobalUserEvent] for [global][CommandScope.GLOBAL] commands, or,
 * [GuildUserEvent] for [global guild-only][CommandScope.GLOBAL_NO_DM] and [guild][CommandScope.GUILD] commands.
 *
 * ### Option types
 * - Input options: Uses [@ContextOption][ContextOption], supported types are in [ParameterResolver],
 * but only the targeted [User][GlobalUserEvent.getTarget]/[Member][GlobalUserEvent.getTargetMember] and [InputUser] are supported by default,
 * additional types can be added by implementing [UserContextParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options and services: No annotation, additional types can be added by implementing [ICustomResolver].
 *
 * @see GlobalUserEvent.getTarget
 * @see GlobalUserEvent.getTargetMember
 *
 * @see Command @Command
 * @see ContextOption @ContextOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Filter @Filter
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager.userCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAUserCommand(
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
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see UserCommandBuilder.isDefaultLocked
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
     * @see UserCommandBuilder.nsfw DSL equivalent
     */
    val nsfw: Boolean = false,

    /**
     * Primary name of the command, which can contain spaces and upper cases.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     */
    val name: String
)
