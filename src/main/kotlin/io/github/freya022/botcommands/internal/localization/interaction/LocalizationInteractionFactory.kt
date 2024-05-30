package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

@BService
internal class LocalizationInteractionFactory internal constructor(
    private val localizationService: LocalizationService,
    private val localizationConfig: BLocalizationConfig,
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
) {
    internal fun create(event: IReplyCallback) =
        LocalizableInteractionImpl(event, localizationService, localizationConfig, userLocaleProvider, guildLocaleProvider)
}