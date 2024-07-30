package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.context.builder.MessageCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.UserCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.TopLevelSlashCommandBuilderImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import kotlin.reflect.KFunction

sealed class AbstractApplicationCommandManager(val context: BContext) {
    private val slashCommandMap: NamedCommandMap<TopLevelSlashCommandInfoImpl> = NamedCommandMap()
    internal val slashCommands: Collection<TopLevelSlashCommandInfoImpl> = slashCommandMap.values

    private val userContextCommandMap: NamedCommandMap<UserCommandInfoImpl> = NamedCommandMap()
    internal val userContextCommands: Collection<UserCommandInfoImpl> = userContextCommandMap.values

    private val messageContextCommandMap: NamedCommandMap<MessageCommandInfoImpl> = NamedCommandMap()
    internal val messageContextCommands: Collection<MessageCommandInfoImpl> = messageContextCommandMap.values

    internal val allApplicationCommands: Collection<TopLevelApplicationCommandInfo>
        get() = slashCommands + userContextCommands + messageContextCommands

    internal abstract val defaultScope: CommandScope

    internal abstract fun isValidScope(scope: CommandScope): Boolean

    protected abstract fun checkScope(scope: CommandScope)

    /**
     * Declares the supplied function as a slash command.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups)
     * on which paths are allowed.
     *
     * ### Requirements
     * - The declaring class must be annotated with [@Command][Command].
     * - First parameter must be [GlobalSlashEvent] for [global][CommandScope.GLOBAL] commands, or,
     * [GuildSlashEvent] for [global guild-only][CommandScope.GLOBAL_NO_DM] and [guild][CommandScope.GUILD] commands.
     *
     * @see Command @Command
     *
     * @see JDASlashCommand @JDASlashCommand
     */
    fun slashCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        checkScope(scope)

        TopLevelSlashCommandBuilderImpl(context, name, function, scope)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(slashCommandMap::putNewCommand)
    }

    /**
     * Declares the supplied function as a user context command.
     *
     * The targeted function must have a [GlobalUserEvent] or a [GuildUserEvent],
     * with the only accepted [options][UserCommandBuilder.option] being [Member], [User] and [InputUser],
     * which will be the *targeted* entity.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#user-commands) for more details.
     *
     * **Requirement:** The declaring class must be annotated with [@Command][Command].
     *
     * @see GlobalUserEvent.getTarget
     * @see GlobalUserEvent.getTargetMember
     *
     * @see Command @Command
     *
     * @see JDAUserCommand @JDAUserCommand
     */
    fun userCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        checkScope(scope)

        UserCommandBuilderImpl(context, name, function, scope)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(userContextCommandMap::putNewCommand)
    }

    /**
     * Declares the supplied function as a message context command.
     *
     * The targeted function must have a [GlobalMessageEvent] or a [GuildMessageEvent],
     * with the only accepted [option][MessageCommandBuilder.option] being [Message],
     * which will be the *targeted* message.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#message-commands) for more details.
     *
     * **Requirement:** The declaring class must be annotated with [@Command][Command].
     *
     * @see GlobalMessageEvent.getTarget
     *
     * @see Command @Command
     *
     * @see JDAMessageCommand
     */
    fun messageCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        checkScope(scope)

        MessageCommandBuilderImpl(context, name, function, scope)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(messageContextCommandMap::putNewCommand)
    }
}