package io.github.freya022.botcommands.api.localization.readers.provider

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.readers.JsonLocalizationMapReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@BService
internal open class DefaultLocalizationMapReaderProvider {
    @Bean("builtinJsonLocalizationMapReader")
    @BService(name = "builtinJsonLocalizationMapReader")
    open fun defaultJsonLocalizationMapReader(context: BContext): JsonLocalizationMapReader {
        return JsonLocalizationMapReader(context, "bc_localization")
    }
}