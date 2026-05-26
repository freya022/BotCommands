package io.github.freya022.botcommands.internal.core.autoconfigure

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService
import net.dv8tion.jda.api.EmbedBuilder

@RequiresTextCommands
@InternalAutoConfiguration
internal open class DefaultEmbedSupplierAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(DefaultEmbedSupplier::class)
    open fun defaultEmbedSupplier(): DefaultEmbedSupplier {
        return DefaultEmbedSupplier(::EmbedBuilder)
    }
}
