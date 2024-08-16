package io.github.freya022.botcommands.api.localization.readers.provider

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@BConfiguration
internal open class DefaultJsonLocalizationMapReaderProvider {
    @Bean("builtinDefaultJsonLocalizationMapReader")
    @BService(name = "builtinDefaultJsonLocalizationMapReader")
    open fun defaultJsonLocalizationMapReader(context: BContext): DefaultJsonLocalizationMapReader {
        return DefaultJsonLocalizationMapReader(context, "bc_localization")
    }
}