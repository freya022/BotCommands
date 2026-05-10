package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.text.LocalizableTextCommand
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import net.dv8tion.jda.api.entities.Message

// Don't require enabled feature, could be used by user's own impl
@BService
internal class LocalizableTextCommandFactory internal constructor(
    private val localizationService: LocalizationService,
    private val localizationConfig: BLocalizationConfig,
    private val localeProvider: MessageLocaleProvider,
    private val messagesFactory: BotCommandsMessagesFactory,
) {
    fun create(message: Message): LocalizableTextCommand =
        LocalizableTextCommandImpl(message, localizationService, localizationConfig, localeProvider, messagesFactory)
}
