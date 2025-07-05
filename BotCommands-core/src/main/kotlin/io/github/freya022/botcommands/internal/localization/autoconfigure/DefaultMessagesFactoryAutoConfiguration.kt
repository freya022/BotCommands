@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.localization.autoconfigure

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class DefaultMessagesFactoryAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(DefaultMessagesFactory::class)
    open fun defaultMessagesFactory(
        localizationService: LocalizationService,
        textCommandLocaleProvider: TextCommandLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): DefaultMessagesFactory {
        return FallbackDefaultMessagesFactory(localizationService, textCommandLocaleProvider, userLocaleProvider)
    }
}
