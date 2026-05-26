package io.github.freya022.botcommands.internal.core.autoconfigure

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@RequiresTextCommands
@InternalAutoConfiguration
internal open class DefaultEmbedFooterIconSupplierAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(DefaultEmbedFooterIconSupplier::class)
    open fun defaultEmbedFooterIconSupplier(): DefaultEmbedFooterIconSupplier {
        return DefaultEmbedFooterIconSupplier { null }
    }
}
