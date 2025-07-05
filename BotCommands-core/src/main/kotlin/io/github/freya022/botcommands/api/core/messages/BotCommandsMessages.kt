package io.github.freya022.botcommands.api.core.messages

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.events.GenericEvent
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
    fun uncaughtException(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when the user is missing [permissions][io.github.freya022.botcommands.api.commands.annotations.UserPermissions]
     */
    fun missingUserPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when the bot is missing [permissions][io.github.freya022.botcommands.api.commands.annotations.BotPermissions]
     */
    fun missingBotPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when a text command is [only usable by the owner][io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner]
     */
    fun ownerOnly(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when a user has exceeded a command's [rate limit][io.github.freya022.botcommands.api.commands.annotations.RateLimit]
     */
    fun userRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel has exceeded a command's [rate limit][io.github.freya022.botcommands.api.commands.annotations.RateLimit]
     */
    fun channelRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a guild has exceeded a command's [rate limit][io.github.freya022.botcommands.api.commands.annotations.RateLimit]
     */
    fun guildRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when application commands are not loaded on the guild yet
     */
    fun applicationCommandsNotAvailable(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when the command is not found
     */
    fun commandNotFound(event: GenericEvent?, suggestions: Collection<TopLevelTextCommandInfo>): MessageCreateData

    /**
     * @return Message to display when a channel parameter could not be resolved
     */
    fun resolverChannelNotFound(event: GenericEvent?, channelId: Long): MessageCreateData

    /**
     * @return Message to display when a channel parameter could be resolved but is not accessible (such as private threads)
     */
    fun resolverChannelMissingAccess(event: GenericEvent?, channelId: Long): MessageCreateData

    /**
     * @return Message to display when a user parameter could not be resolved
     */
    fun resolverUserNotFound(event: GenericEvent?, userId: Long): MessageCreateData

    /**
     * @return Message to display when a slash command option is unresolvable (only in slash command interactions)
     */
    fun slashCommandUnresolvableOption(event: GenericEvent?, option: SlashCommandOption): MessageCreateData

    /**
     * @return Message to display when a User's DMs are closed (when sending help content for example)
     */
    fun closedDirectMessages(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when a command is used in a NSFW [IAgeRestrictedChannel] (see [@NSFW][io.github.freya022.botcommands.api.commands.text.annotations.NSFW])
     */
    fun nsfwOnly(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when a user tries to use a component it isn't allowed to interact with
     */
    fun componentNotAllowed(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when a user tries to use a component which does not exist anymore
     */
    fun componentExpired(event: GenericEvent?): MessageCreateData

    /**
     * @return Message to display when a user tries to use a modal which has reached timeout
     */
    fun modalExpired(event: GenericEvent?): MessageCreateData
}