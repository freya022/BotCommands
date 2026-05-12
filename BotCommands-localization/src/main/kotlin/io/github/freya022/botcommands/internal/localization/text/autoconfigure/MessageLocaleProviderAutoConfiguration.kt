package io.github.freya022.botcommands.internal.localization.text.autoconfigure

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class MessageLocaleProviderAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(MessageLocaleProvider::class)
    open fun messageLocaleProvider(): MessageLocaleProvider {
        return DefaultMessageLocaleProvider
    }
}
