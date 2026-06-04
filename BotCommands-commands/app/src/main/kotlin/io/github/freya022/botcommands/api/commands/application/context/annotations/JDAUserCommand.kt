package io.github.freya022.botcommands.api.commands.application.context.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplierProvider
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.provider.*
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.ratelimit.annotations.Cooldown
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimit
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a user context command.
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#user-commands)
 * for more details.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command].
 *
 * The first parameter must be:
 * - [GuildUserEvent] if the [interaction context][contexts]
 * only contains [InteractionContextType.GUILD].
 * - [GlobalUserEvent] in other cases.
 *
 * ### Option types
 * - Input options: Uses [@ContextOption][ContextOption].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Generated options: Use [@GeneratedOption][GeneratedOption], implement [ApplicationGeneratedValueSupplierProvider]
 *   and return, on the correct command path/option name,
 *   an appropriate supplier that will generate an object of the correct type.
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * ### Permissions
 *
 * Required user/bot permissions can be set with [@UserPermissions][UserPermissions]/[@BotPermissions][BotPermissions].
 *
 * ### Declaration filtering
 *
 * You can prevent registration of **guild** commands with [@DeclarationFilter][DeclarationFilter].
 *
 * ### Execution filtering
 *
 * You can arbitrarily prevent execution of commands by using [@Filter][Filter],
 * passing an implementation of [ApplicationCommandFilter].
 *
 * ### Rate limiting
 *
 * See [@RateLimit][RateLimit] / [@Cooldown][Cooldown],
 * you can also apply a custom rate limiter using [@RateLimitReference][RateLimitReference].
 *
 * @see GlobalUserEvent.getTarget
 * @see GlobalUserEvent.getTargetMember
 *
 * @see GlobalApplicationCommandProvider Declaring global application commands using the DSL
 * @see GuildApplicationCommandProvider Declaring guild application commands using the DSL
 * @see AbstractApplicationCommandManager.userCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAUserCommand(
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
     * Primary name of the command, which can contain spaces and upper cases.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     */
    val name: String
)
