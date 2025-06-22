package io.github.freya022.botcommands.api.core.replies

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.core.replies.exceptions.MissingReplyTemplateException
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.localize
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.to

/**
 * Default implementation of [BuiltinReplies],
 * see [DefaultBuiltinRepliesFactory] for more details.
 *
 * @see DefaultBuiltinRepliesFactory
 */
open class DefaultBuiltinReplies(
    protected val permissionLocalization: PermissionLocalization,
    localizationService: LocalizationService,
    protected val locale: Locale,
    bundleName: String,
) : BuiltinReplies {

    protected val localization: Localization = localizationService.getInstance(bundleName, locale)
        ?: throwArgument("Could not find localization files for '$bundleName'")

    override fun uncaughtException(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("uncaught_exception").localize().toMessage()
    }

    override fun missingUserPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.user").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun missingBotPermissions(event: GenericEvent?, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.bot").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun ownerOnly(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("owner_only").localize().toMessage()
    }

    override fun userRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        val args = "timestamp" to TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.user").localize(args).toMessage()
    }

    override fun channelRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.channel").localize("timestamp" to timestamp).toMessage()
    }

    override fun guildRateLimited(event: GenericEvent?, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.guild").localize("timestamp" to timestamp).toMessage()
    }

    override fun applicationCommandsNotAvailable(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("commands.application.not_available").localize().toMessage()
    }

    override fun commandNotFound(event: GenericEvent?, suggestions: Collection<TopLevelTextCommandInfo>): MessageCreateData {
        val suggestionsStr = suggestions.joinToString(separator = "**, **", prefix = "**", postfix = "**") { it.name }
        return getLocalizationTemplate("commands.text.not_found").localize("suggestions" to suggestionsStr).toMessage()
    }

    override fun resolverChannelNotFound(event: GenericEvent?, channelId: Long): MessageCreateData {
        return getLocalizationTemplate("resolver.channel.not_found").localize("channelId" to channelId).toMessage()
    }

    override fun resolverChannelMissingAccess(event: GenericEvent?, channelId: Long): MessageCreateData {
        return getLocalizationTemplate("resolver.channel.missing_access").localize("channelId" to channelId).toMessage()
    }

    override fun resolverUserNotFound(event: GenericEvent?, userId: Long): MessageCreateData {
        return getLocalizationTemplate("resolver.user.not_found").localize("userId" to userId).toMessage()
    }

    override fun slashCommandUnresolvableOption(event: GenericEvent?, option: SlashCommandOption): MessageCreateData {
        return getLocalizationTemplate("commands.slash.option.unresolvable").localize("optionName" to option.discordName).toMessage()
    }

    override fun closedDirectMessages(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("direct_messages.closed").localize().toMessage()
    }

    override fun nsfwOnly(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("nsfw_only").localize().toMessage()
    }

    override fun componentNotAllowed(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("components.not_allowed").localize().toMessage()
    }

    override fun componentExpired(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("components.expired").localize().toMessage()
    }

    override fun modalExpired(event: GenericEvent?): MessageCreateData {
        return getLocalizationTemplate("modals.expired").localize().toMessage()
    }

    protected fun getLocalizationTemplate(path: String): LocalizationTemplate {
        val template = localization[path]
            ?: throw MissingReplyTemplateException("Template '$path' could not be found, available keys: ${localization.keys}")

        return template
    }

    protected fun String.toMessage(): MessageCreateData = MessageCreateData.fromContent(this)
}