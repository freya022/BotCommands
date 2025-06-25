package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class TextCommandLocaleProviderAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(TextCommandLocaleProvider::class)
    open fun textCommandLocaleProvider(): TextCommandLocaleProvider {
        return DefaultTextCommandLocaleProvider
    }
}
