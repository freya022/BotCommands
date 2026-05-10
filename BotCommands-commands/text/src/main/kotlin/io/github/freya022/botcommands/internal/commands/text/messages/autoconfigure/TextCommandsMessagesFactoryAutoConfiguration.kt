package io.github.freya022.botcommands.internal.commands.text.messages.autoconfigure

import io.github.freya022.botcommands.api.commands.text.messages.DefaultTextCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class TextCommandsMessagesFactoryAutoConfiguration internal constructor() {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(TextCommandsMessagesFactory::class)
    open fun textCommandsMessagesFactory(
        permissionLocalization: PermissionLocalization,
        localizationService: LocalizationService,
        messageLocaleProvider: MessageLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): TextCommandsMessagesFactory {
        return DefaultTextCommandsMessagesFactory(
            permissionLocalization,
            localizationService,
            messageLocaleProvider,
            userLocaleProvider
        )
    }
}
