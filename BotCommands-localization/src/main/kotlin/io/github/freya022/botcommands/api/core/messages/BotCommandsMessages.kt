package io.github.freya022.botcommands.api.core.messages

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant

/**
 * Returns the messages used by the framework, instances are produced by [BotCommandsMessagesFactory].
 *
 * @see BotCommandsMessagesFactory
 */
interface BotCommandsMessages {

    /**
     * @return Message to display when an uncaught exception occurs
     */
    fun uncaughtException(event: GenericEvent): MessageCreateData

    /**
     * @return Message to display when the bot is missing permissions
     */
    fun missingBotPermissions(event: GenericEvent, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when a user has exceeded a command's rate limit
     */
    fun userRateLimited(event: GenericEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel has exceeded a command's rate limit
     */
    fun channelRateLimited(event: GenericEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a guild has exceeded a command's rate limit
     */
    fun guildRateLimited(event: GenericEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel parameter could not be resolved
     */
    fun resolverChannelNotFound(event: GenericEvent, channelId: Long): MessageCreateData

    /**
     * @return Message to display when a channel parameter could be resolved but is not accessible (such as private threads)
     */
    fun resolverChannelMissingAccess(event: GenericEvent, channelId: Long): MessageCreateData

    /**
     * @return Message to display when a user parameter could not be resolved
     */
    fun resolverUserNotFound(event: GenericEvent, userId: Long): MessageCreateData

    /**
     * @return Message to display when a user tries to use a component it isn't allowed to interact with
     */
    fun componentNotAllowed(event: GenericComponentInteractionCreateEvent): MessageCreateData

    /**
     * @return Message to display when a user tries to use a component which does not exist anymore
     */
    fun componentExpired(event: GenericComponentInteractionCreateEvent): MessageCreateData

    /**
     * @return Message to display when a user tries to use a modal which has reached timeout
     */
    fun modalExpired(event: ModalInteractionEvent): MessageCreateData
}
