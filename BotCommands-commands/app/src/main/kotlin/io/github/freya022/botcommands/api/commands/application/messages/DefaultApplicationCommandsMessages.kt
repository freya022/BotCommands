package io.github.freya022.botcommands.api.commands.application.messages

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.messages.exceptions.MissingMessageTemplateException
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.localize
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.to

/**
 * Default implementation of [ApplicationCommandsMessages],
 * see [DefaultApplicationCommandsMessagesFactory] for more details.
 *
 * @see DefaultApplicationCommandsMessagesFactory
 */
open class DefaultApplicationCommandsMessages(
    protected val permissionLocalization: PermissionLocalization,
    localizationService: LocalizationService,
    protected val locale: Locale,
    bundleName: String,
    commonBundleName: String
) : ApplicationCommandsMessages {

    protected val localization: Localization = localizationService.getInstance(bundleName, locale)
        ?: throwArgument("Could not find localization files for '$bundleName'")
    protected val commonLocalization: Localization = localizationService.getInstance(commonBundleName, locale)
        ?: throwArgument("Could not find localization files for '$commonBundleName'")

    override fun uncaughtException(event: GenericCommandInteractionEvent): MessageCreateData {
        return getLocalizationTemplate("uncaught_exception").localize().toMessage()
    }

    override fun missingUserPermissions(event: GenericCommandInteractionEvent, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.user").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun missingBotPermissions(event: GenericCommandInteractionEvent, permissions: Set<Permission>): MessageCreateData {
        val localizedPermissions = permissions.joinToString(separator = ", ") { permissionLocalization.localize(it, locale) }
        return getLocalizationTemplate("missing.permissions.bot").localize("permissions" to localizedPermissions).toMessage()
    }

    override fun userRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData {
        val args = "timestamp" to TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.user").localize(args).toMessage()
    }

    override fun channelRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.channel").localize("timestamp" to timestamp).toMessage()
    }

    override fun guildRateLimited(event: GenericCommandInteractionEvent, deadline: Instant): MessageCreateData {
        val timestamp = TimeFormat.RELATIVE.atInstant(deadline)
        return getLocalizationTemplate("ratelimited.guild").localize("timestamp" to timestamp).toMessage()
    }

    override fun applicationCommandsNotAvailable(event: GenericCommandInteractionEvent): MessageCreateData {
        return getLocalizationTemplate("commands.application.not_available").localize().toMessage()
    }

    override fun slashCommandUnresolvableOption(event: CommandInteractionPayload, option: SlashCommandOption): MessageCreateData {
        return getLocalizationTemplate("commands.slash.option.unresolvable").localize("option_name" to option.discordName).toMessage()
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
