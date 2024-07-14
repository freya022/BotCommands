package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

// Don't require enabled feature, could be used by user's own impl
@BService
internal class LocalizableTextCommandFactory internal constructor(
    private val localizationService: LocalizationService,
    private val localizationConfig: BLocalizationConfig,
    private val localeProvider: TextCommandLocaleProvider,
    private val defaultMessagesFactory: DefaultMessagesFactory,
) {
    internal fun create(event: MessageReceivedEvent) =
        LocalizableTextCommandImpl(event, localizationService, localizationConfig, localeProvider, defaultMessagesFactory)
}