package io.github.freya022.botcommands.api.commands.application.context.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.provider.*
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a message context command.
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#message-commands)
 * for more details.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command] and extend [ApplicationCommand].
 *
 * The first parameter must be:
 * - [GuildMessageEvent] if the [interaction context][contexts]
 * only contains [InteractionContextType.GUILD].
 * - [GlobalMessageEvent] in other cases.
 *
 * ### Option types
 * - Input options: Uses [@ContextOption][ContextOption], supported types and modifiers are in [ParameterResolver],
 * but only the targeted [Message][GlobalMessageEvent.getTarget] is supported by default,
 * additional types can be added by implementing [MessageContextParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * @see GlobalMessageEvent.getTarget
 *
 * @see Command @Command
 * @see ContextOption @ContextOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 * @see Filter @Filter
 *
 * @see GlobalApplicationCommandProvider Declaring global application commands using the DSL
 * @see GuildApplicationCommandProvider Declaring guild application commands using the DSL
 * @see AbstractApplicationCommandManager.messageCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAMessageCommand(
    /**
     * Specifies the application command scope for this command, where the command will be pushed to.
     *
     * **Default:** [CommandScope.GLOBAL]
     */
    val scope: CommandScope = CommandScope.GLOBAL,

    /**
     * The interaction contexts in which this command is executable in,
     * think of it as 'Where can I use this command in the Discord client'.
     *
     * **Default, depending on [scope]:**
     * - [Global][CommandScope.GLOBAL] : [GlobalApplicationCommandManager.Defaults.contexts]
     * - [Guild][CommandScope.GUILD] : [GuildApplicationCommandManager.Defaults.contexts]
     *
     * @see InteractionContextType
     * @see MessageCommandBuilder.contexts
     */
    val contexts: Array<out InteractionContextType> = [],

    /**
     * The integration types in which this command can be installed in.
     *
     * **Default, depending on [scope]:**
     * - [Global][CommandScope.GLOBAL] : [GlobalApplicationCommandManager.Defaults.integrationTypes]
     * - [Guild][CommandScope.GUILD] : [GuildApplicationCommandManager.Defaults.integrationTypes]
     *
     * @see IntegrationType
     * @see MessageCommandBuilder.integrationTypes
     */
    val integrationTypes: Array<out IntegrationType> = [],

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** You cannot use this with [@UserPermissions][UserPermissions].
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see MessageCommandBuilder.isDefaultLocked DSL equivalent
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
     * @see MessageCommandBuilder.nsfw DSL equivalent
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
