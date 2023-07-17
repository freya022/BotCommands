package com.freya02.botcommands.api.commands.application.context.annotations

import com.freya02.botcommands.api.commands.annotations.BotPermissions
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a message context command.
 *
 * The targeted method must have a [GlobalMessageEvent] or a [GuildMessageEvent],
 * with the only accepted [option][ContextOption] being [Message],
 * which will be the *targeted* message.
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#message-commands) for more details.
 *
 * **Requirement:** The declaring class must be annotated with [@Command][Command].
 *
 * @see GlobalMessageEvent.getTarget
 *
 * @see Command @Command
 * @see ContextOption @ContextOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager.messageCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAMessageCommand(
    /**
     * Specifies the application command scope for this command.
     *
     * **Default:** [CommandScope.GLOBAL_NO_DM]
     *
     * @see CommandScope DSL equivalent
     */
    val scope: CommandScope = CommandScope.GLOBAL_NO_DM,

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** you cannot use this with [UserPermissions].
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see MessageCommandBuilder.isDefaultLocked
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
