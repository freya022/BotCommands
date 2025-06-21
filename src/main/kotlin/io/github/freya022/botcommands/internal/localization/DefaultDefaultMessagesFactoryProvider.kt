@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.utils.classRef
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// I hate those names
@Configuration
@BService
internal open class DefaultDefaultMessagesFactoryProvider {
    @Bean
    @ConditionalOnMissingBean(DefaultMessagesFactory::class)
    @BService
    @ConditionalService(ActivationCondition::class)
    internal open fun defaultDefaultMessagesFactory(
        localizationService: LocalizationService,
        textCommandLocaleProvider: TextCommandLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): DefaultMessagesFactory = FallbackDefaultMessagesFactory(localizationService, textCommandLocaleProvider, userLocaleProvider)

    internal object ActivationCondition : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            val types = serviceContainer.getInterfacedServiceTypes<DefaultMessagesFactory>()
            if (types.isNotEmpty()) {
                return "An user supplied ${classRef<DefaultMessagesFactory>()} is already active (${types.first().simpleNestedName})"
            }

            return null
        }
    }
}