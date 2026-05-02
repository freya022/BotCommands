package io.github.freya022.botcommands.api.commands.text.messages

import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.core.messages.exceptions.MissingMessageTemplateException
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.localize
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.to

/**
 * Default implementation of [TextCommandsMessages],
 * see [DefaultTextCommandsMessagesFactory] for more details.
 *
 * @see DefaultTextCommandsMessagesFactory
 */
open class DefaultTextCommandsMessages(
    protected val permissionLocalization: PermissionLocalization,
    localizationService: LocalizationService,
    protected val locale: Locale,
    bundleName: String,
    commonBundleName: String
) : TextCommandsMessages {

    protected val localization: Localization = localizationService.getInstance(bundleName, locale)
        ?: throwArgument("Could not find localization files for '$bundleName'")
    protected val commonLocalization: Localization = localizationService.getInstance(commonBundleName, locale)
        ?: throwArgument("Could not find localization files for '$commonBundleName'")

    override fun uncaughtException(event: MessageReceivedEvent): MessageCreateData {
        return getLocalizationTemplate("uncaught_exception").localize().toMessage()
    }

    override fun missingUserPermissions(event: MessageReceivedEvent, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.user").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun missingBotPermissions(event: MessageReceivedEvent, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.bot").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun ownerOnly(event: MessageReceivedEvent): MessageCreateData {
        return getLocalizationTemplate("owner_only").localize().toMessage()
    }

    override fun userRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData {
        val args = "timestamp" to TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.user").localize(args).toMessage()
    }

    override fun channelRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.channel").localize("timestamp" to timestamp).toMessage()
    }

    override fun guildRateLimited(event: MessageReceivedEvent, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.guild").localize("timestamp" to timestamp).toMessage()
    }

    override fun resolverChannelMissingAccess(event: MessageReceivedEvent, channelId: Long): MessageCreateData {
        return getLocalizationTemplate("resolver.channel.missing_access").localize("channel_id" to channelId).toMessage()
    }

    override fun commandNotFound(event: MessageReceivedEvent, suggestions: Collection<TopLevelTextCommandInfo>): MessageCreateData {
        val suggestionsStr = suggestions.joinToString(separator = "**, **", prefix = "**", postfix = "**") { it.name }
        return getLocalizationTemplate("commands.text.not_found").localize("suggestions" to suggestionsStr).toMessage()
    }

    override fun closedDirectMessages(event: MessageReceivedEvent): MessageCreateData {
        return getLocalizationTemplate("direct_messages.closed").localize().toMessage()
    }

    override fun nsfwOnly(event: MessageReceivedEvent): MessageCreateData {
        return getLocalizationTemplate("nsfw_only").localize().toMessage()
    }

    protected fun getLocalizationTemplate(path: String): LocalizationTemplate {
        val template = localization[path]
            ?: commonLocalization[path]
            ?: throw MissingMessageTemplateException("Template '$path' could not be found, available keys: ${localization.keys} / ${commonLocalization.keys}")

        return template
    }

    protected fun String.toMessage(): MessageCreateData =
        MessageCreateBuilder()
            .setContent(this)
            .useComponentsV2(false)
            .build()
}
