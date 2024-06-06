package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.text.LocalizableTextCommand
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.localization.AbstractLocalizableAction
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import java.util.*

internal class LocalizableTextCommandImpl internal constructor(
    private val event: MessageReceivedEvent,
    localizationService: LocalizationService,
    localizationConfig: BLocalizationConfig,
    localeProvider: TextCommandLocaleProvider,
) : AbstractLocalizableAction(localizationConfig, localizationService), LocalizableTextCommand {
    private val locale: Locale by lazy { localeProvider.getLocale(event) }

    override fun getLocalizationContext(bundleName: String, pathPrefix: String): TextLocalizationContext {
        TODO()
    }

    override fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String {
        return getLocalizedMessage(locale, localizationPath, *entries)
    }

    override fun respondGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction {
        return event.channel.sendMessage(getGuildMessage(localizationPath, *entries))
    }

    override fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction {
        return event.message.reply(getGuildMessage(localizationPath, *entries))
    }

    override fun respondLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): MessageCreateAction {
        return event.channel.sendMessage(getLocalizedMessage(locale, localizationPath, *entries))
    }

    override fun replyLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): MessageCreateAction {
        return event.message.reply(getLocalizedMessage(locale, localizationPath, *entries))
    }
}