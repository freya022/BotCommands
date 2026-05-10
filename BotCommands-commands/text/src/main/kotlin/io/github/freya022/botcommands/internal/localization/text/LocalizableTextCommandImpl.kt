package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.commands.text.localization.context.setGuildLocaleProvider
import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.text.LocalizableTextCommand
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import io.github.freya022.botcommands.internal.localization.AbstractLocalizableAction
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import java.util.Locale

internal class LocalizableTextCommandImpl internal constructor(
    private val message: Message,
    localizationService: LocalizationService,
    localizationConfig: BLocalizationConfig,
    private val localeProvider: MessageLocaleProvider,
    private val messagesFactory: BotCommandsMessagesFactory,
) : AbstractLocalizableAction(localizationConfig, localizationService), LocalizableTextCommand {
    private val locale: Locale by lazy { localeProvider.getLocale(message) }

    override fun getLocalizationContext(bundleName: String, pathPrefix: String?): TextLocalizationContext {
        return LocalizationContext.builder(localizationService, bundleName)
            .setPrefix(pathPrefix)
            .setGuildLocaleProvider(localeProvider, message)
            .build()
    }

    @Deprecated("Built-in messages are not longer in a single bundle, get an instance from a BotCommandsMessagesFactory")
    override fun getBotCommandsMessages(): BotCommandsMessages {
        return messagesFactory.get(locale)
    }

    override fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String {
        return getLocalizedMessage(locale, localizationPath, *entries)
    }

    override fun respondGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction {
        return message.channel.sendMessage(getGuildMessage(localizationPath, *entries)).useComponentsV2(false)
    }

    override fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction {
        return message.reply(getGuildMessage(localizationPath, *entries)).useComponentsV2(false)
    }

    override fun respondLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): MessageCreateAction {
        return message.channel.sendMessage(getLocalizedMessage(locale, localizationPath, *entries)).useComponentsV2(false)
    }

    override fun replyLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): MessageCreateAction {
        return message.reply(getLocalizedMessage(locale, localizationPath, *entries)).useComponentsV2(false)
    }
}
