@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.core.replies

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.core.replies.BotCommandsMessages
import io.github.freya022.botcommands.api.localization.DefaultMessages
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant

internal class BotCommandsMessagesDefaultMessagesAdapter internal constructor(
    private val defaultMessages: DefaultMessages,
) : BotCommandsMessages {

    override fun uncaughtException(event: GenericEvent?): MessageCreateData {
        return defaultMessages.generalErrorMsg.toMessage()
    }

    override fun missingUserPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData {
        return defaultMessages.getUserPermErrorMsg(permissions).toMessage()
    }

    override fun missingBotPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData {
        return defaultMessages.getBotPermErrorMsg(permissions).toMessage()
    }

    override fun ownerOnly(event: GenericEvent?): MessageCreateData {
        return defaultMessages.ownerOnlyErrorMsg.toMessage()
    }

    override fun userRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        return defaultMessages.getUserRateLimitMsg(TimeFormat.RELATIVE.atInstant(deadline)).toMessage()
    }

    override fun channelRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        return defaultMessages.getChannelRateLimitMsg(TimeFormat.RELATIVE.atInstant(deadline)).toMessage()
    }

    override fun guildRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        return defaultMessages.getGuildRateLimitMsg(TimeFormat.RELATIVE.atInstant(deadline)).toMessage()
    }

    override fun applicationCommandsNotAvailable(event: GenericEvent?): MessageCreateData {
        return defaultMessages.applicationCommandsNotAvailableMsg.toMessage()
    }

    override fun commandNotFound(event: GenericEvent?, suggestions: Collection<TopLevelTextCommandInfo>): MessageCreateData {
        val suggestionsStr = suggestions.joinToString(separator = "**, **", prefix = "**", postfix = "**") { it.name }
        return defaultMessages.getCommandNotFoundMsg(suggestionsStr).toMessage()
    }

    override fun resolverChannelNotFound(event: GenericEvent?, channelId: Long): MessageCreateData {
        return defaultMessages.resolverChannelNotFoundMsg.toMessage()
    }

    override fun resolverChannelMissingAccess(event: GenericEvent?, channelId: Long): MessageCreateData {
        return defaultMessages.getResolverChannelMissingAccessMsg("<#$channelId>").toMessage()
    }

    override fun resolverUserNotFound(event: GenericEvent?, userId: Long): MessageCreateData {
        return defaultMessages.resolverUserNotFoundMsg.toMessage()
    }

    override fun slashCommandUnresolvableOption(event: GenericEvent?, option: SlashCommandOption): MessageCreateData {
        return defaultMessages.getSlashCommandUnresolvableOptionMsg(option.discordName).toMessage()
    }

    override fun closedDirectMessages(event: GenericEvent?): MessageCreateData {
        return defaultMessages.closedDMErrorMsg.toMessage()
    }

    override fun nsfwOnly(event: GenericEvent?): MessageCreateData {
        return defaultMessages.nsfwOnlyErrorMsg.toMessage()
    }

    override fun componentNotAllowed(event: GenericEvent?): MessageCreateData {
        return defaultMessages.componentNotAllowedErrorMsg.toMessage()
    }

    override fun componentExpired(event: GenericEvent?): MessageCreateData {
        return defaultMessages.componentExpiredErrorMsg.toMessage()
    }

    override fun modalExpired(event: GenericEvent?): MessageCreateData {
        return defaultMessages.modalExpiredErrorMsg.toMessage()
    }

    private fun String.toMessage(): MessageCreateData = MessageCreateData.fromContent(this)
}