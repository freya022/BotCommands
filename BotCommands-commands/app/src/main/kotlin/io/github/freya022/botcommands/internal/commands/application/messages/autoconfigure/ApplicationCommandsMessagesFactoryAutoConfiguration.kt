package io.github.freya022.botcommands.internal.commands.application.messages.autoconfigure

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.messages.ApplicationCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.application.messages.DefaultApplicationCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
@RequiresApplicationCommands
internal open class ApplicationCommandsMessagesFactoryAutoConfiguration internal constructor() {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(ApplicationCommandsMessagesFactory::class)
    open fun applicationCommandsMessagesFactory(
        permissionLocalization: PermissionLocalization,
        localizationService: LocalizationService,
        userLocaleProvider: UserLocaleProvider,
    ): ApplicationCommandsMessagesFactory {
        return DefaultApplicationCommandsMessagesFactory(
            permissionLocalization,
            localizationService,
            userLocaleProvider
        )
    }
}
