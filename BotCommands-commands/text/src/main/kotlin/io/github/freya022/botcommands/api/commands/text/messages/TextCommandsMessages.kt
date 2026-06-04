package io.github.freya022.botcommands.api.commands.text.messages

import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant

/**
 * Returns the messages used by the text commands module, instances are produced by [TextCommandsMessagesFactory].
 *
 * @see TextCommandsMessagesFactory
 */
interface TextCommandsMessages {

    /**
     * @return Message to display when an uncaught exception occurs
     */
    fun uncaughtException(event: MessageReceivedEvent): MessageCreateData

    /**
     * @return Message to display when the user is missing [permissions][io.github.freya022.botcommands.api.commands.annotations.UserPermissions]
     */
    fun missingUserPermissions(event: MessageReceivedEvent, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when the bot is missing [permissions][io.github.freya022.botcommands.api.commands.annotations.BotPermissions]
     */
    fun missingBotPermissions(event: MessageReceivedEvent, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when a text command is [only usable by the owner][io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner]
     */
    fun ownerOnly(event: MessageReceivedEvent): MessageCreateData

    /**
     * @return Message to display when a user has exceeded a command's rate limit
     */
    fun userRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel has exceeded a command's rate limit
     */
    fun channelRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a guild has exceeded a command's rate limit
     */
    fun guildRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel parameter could be resolved but is not accessible (such as private threads)
     */
    fun resolverChannelMissingAccess(event: MessageReceivedEvent, channelId: Long): MessageCreateData

    /**
     * @return Message to display when a text command cannot be found
     */
    fun commandNotFound(event: MessageReceivedEvent, suggestions: Collection<TopLevelTextCommandInfo>): MessageCreateData

    /**
     * @return Message to display when a User's DMs are closed (when sending help content for example)
     */
    fun closedDirectMessages(event: MessageReceivedEvent): MessageCreateData

    /**
     * @return Message to display when a command is used in a NSFW [IAgeRestrictedChannel] (see [@NSFW][io.github.freya022.botcommands.api.commands.text.annotations.NSFW])
     */
    fun nsfwOnly(event: MessageReceivedEvent): MessageCreateData
}
