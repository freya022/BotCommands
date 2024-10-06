package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.builder.MessageCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.builder.UserCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.TopLevelSlashCommandBuilderImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
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

    internal abstract val supportedContexts: Set<InteractionContextType>
    internal abstract val supportedIntegrationTypes: Set<IntegrationType>
    /**
     * Default value of [TopLevelApplicationCommandBuilder.contexts].
     */
    abstract val defaultContexts: Set<InteractionContextType>
    /**
     * Default value of [TopLevelApplicationCommandBuilder.integrationTypes].
     */
    abstract val defaultIntegrationTypes: Set<IntegrationType>

    /**
     * Declares the supplied function as a slash command.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups)
     * on which paths are allowed.
     *
     * The default allowed [interaction contexts][InteractionContextType] and [integration types][IntegrationType]
     * can be redefined in the corresponding command manager.
     *
     * ### Requirements
     * The first parameter must be:
     * - [GuildSlashEvent] if the interaction context only contains [InteractionContextType.GUILD].
     * - [GlobalSlashEvent] in other cases.
     *
     * @see JDASlashCommand @JDASlashCommand
     */
    fun slashCommand(name: String, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        TopLevelSlashCommandBuilderImpl(this, name, function)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(slashCommandMap::putNewCommand)
    }

    @Deprecated(message = "Use overload without CommandScope, optionally set the interaction contexts in the builder")
    fun slashCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        return slashCommand(name, function) {
            contexts = scope.toInteractionContexts()

            builder()
        }
    }

    /**
     * Declares the supplied function as a user context command.
     *
     * The targeted function must have a [GlobalUserEvent] or a [GuildUserEvent],
     * with the only accepted [options][UserCommandBuilder.option] being [Member], [User] and [InputUser],
     * which will be the *targeted* entity.
     *
     * ### Requirements
     * The first parameter must be:
     * - [GuildUserEvent] if the interaction context only contains [InteractionContextType.GUILD].
     * - [GlobalUserEvent] in other cases.
     *
     * The default allowed [interaction contexts][InteractionContextType] and [integration types][IntegrationType]
     * can be redefined in the corresponding command manager.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#user-commands)
     * for more details.
     *
     * @see GlobalUserEvent.getTarget
     * @see GlobalUserEvent.getTargetMember
     *
     * @see JDAUserCommand @JDAUserCommand
     */
    fun userCommand(name: String, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        UserCommandBuilderImpl(this, name, function)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(userContextCommandMap::putNewCommand)
    }

    @Deprecated(message = "Use overload without CommandScope, optionally set the interaction contexts in the builder")
    fun userCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        return userCommand(name, function) {
            contexts = scope.toInteractionContexts()

            builder()
        }
    }

    /**
     * Declares the supplied function as a message context command.
     *
     * The targeted function must have a [GlobalMessageEvent] or a [GuildMessageEvent],
     * with the only accepted [option][MessageCommandBuilder.option] being [Message],
     * which will be the *targeted* message.
     *
     * ### Requirements
     * The first parameter must be:
     * - [GuildMessageEvent] if the interaction context only contains [InteractionContextType.GUILD].
     * - [GlobalMessageEvent] in other cases.
     *
     * The default allowed [interaction contexts][InteractionContextType] and [integration types][IntegrationType]
     * can be redefined in the corresponding command manager.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#message-commands)
     * for more details.
     *
     * @see GlobalMessageEvent.getTarget
     *
     * @see JDAMessageCommand
     */
    fun messageCommand(name: String, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        MessageCommandBuilderImpl(this, name, function)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(messageContextCommandMap::putNewCommand)
    }

    @Deprecated(message = "Use overload without CommandScope, optionally set the interaction contexts in the builder")
    fun messageCommand(name: String, scope: CommandScope = defaultScope, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        return messageCommand(name, function) {
            contexts = scope.toInteractionContexts()

            builder()
        }
    }

    @Suppress("DEPRECATION")
    private fun CommandScope.toInteractionContexts(): Set<InteractionContextType> = when (this) {
        CommandScope.GUILD, CommandScope.GLOBAL_NO_DM -> enumSetOf(InteractionContextType.GUILD)
        CommandScope.GLOBAL -> enumSetOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
    }

    private fun areContextsValid(contexts: Set<InteractionContextType>): Boolean =
        contexts.all { it in supportedContexts }

    internal fun checkContexts(contexts: Set<InteractionContextType>) {
        require(contexts.isNotEmpty()) {
            "Contexts cannot be empty"
        }
        require(areContextsValid(contexts)) {
            "${this.javaClass.simpleNestedName} only accepts the following interaction contexts $supportedContexts"
        }
    }

    private fun areIntegrationsValid(integrationTypes: Set<IntegrationType>): Boolean =
        integrationTypes.all { it in supportedIntegrationTypes }

    internal fun checkIntegrations(integrationTypes: Set<IntegrationType>) {
        require(integrationTypes.isNotEmpty()) {
            "Contexts cannot be empty"
        }
        require(areIntegrationsValid(integrationTypes)) {
            "${this.javaClass.simpleNestedName} only accepts the following integration types $supportedIntegrationTypes"
        }
    }
}