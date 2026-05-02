package io.github.freya022.botcommands.api.commands.application.messages

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant

/**
 * Returns the messages used by the application commands module, instances are produced by [ApplicationCommandsMessagesFactory].
 *
 * @see ApplicationCommandsMessagesFactory
 */
interface ApplicationCommandsMessages {

    /**
     * @return Message to display when an uncaught exception occurs
     */
    fun uncaughtException(event: GenericCommandInteractionEvent): MessageCreateData

    /**
     * @return Message to display when the user is missing [permissions][UserPermissions]
     */
    fun missingUserPermissions(event: GenericCommandInteractionEvent, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when the bot is missing [permissions][BotPermissions]
     */
    fun missingBotPermissions(event: GenericCommandInteractionEvent, permissions: Set<Permission>): MessageCreateData

    /**
     * @return Message to display when a user has exceeded a command's [rate limit][RateLimit]
     */
    fun userRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a channel has exceeded a command's [rate limit][RateLimit]
     */
    fun channelRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when a guild has exceeded a command's [rate limit][RateLimit]
     */
    fun guildRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData

    /**
     * @return Message to display when application commands are not loaded on the guild yet
     */
    fun applicationCommandsNotAvailable(event: GenericCommandInteractionEvent): MessageCreateData

    /**
     * @return Message to display when a slash command option is unresolvable (only in slash command interactions)
     */
    fun slashCommandUnresolvableOption(event: CommandInteractionPayload, option: SlashCommandOption): MessageCreateData
}
