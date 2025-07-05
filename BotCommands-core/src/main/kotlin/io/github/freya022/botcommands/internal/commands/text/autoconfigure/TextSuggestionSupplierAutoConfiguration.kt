package io.github.freya022.botcommands.internal.commands.text.autoconfigure

import io.github.freya022.botcommands.api.commands.text.TextSuggestionSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@RequiresTextCommands
@InternalAutoConfiguration
internal open class TextSuggestionSupplierAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(TextSuggestionSupplier::class)
    open fun textSuggestionSupplier(): TextSuggestionSupplier {
        return DefaultTextSuggestionSupplier()
    }
}
