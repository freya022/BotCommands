package io.github.freya022.botcommands.internal.core.messages.autoconfigure

import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.messages.DefaultBotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class BotCommandsMessagesFactoryAutoConfiguration internal constructor() {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(BotCommandsMessagesFactory::class)
    open fun botCommandsMessagesFactory(
        permissionLocalization: PermissionLocalization,
        localizationService: LocalizationService,
        textCommandLocaleProvider: TextCommandLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): BotCommandsMessagesFactory {
        return DefaultBotCommandsMessagesFactory(
            permissionLocalization,
            localizationService,
            textCommandLocaleProvider,
            userLocaleProvider
        )
    }
}
