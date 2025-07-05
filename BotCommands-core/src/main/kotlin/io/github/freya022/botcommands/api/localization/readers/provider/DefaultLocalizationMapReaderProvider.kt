package io.github.freya022.botcommands.api.localization.readers.provider

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@BService
internal open class DefaultLocalizationMapReaderProvider {
    @Bean("builtinJsonLocalizationMapReader")
    @BService(name = "builtinJsonLocalizationMapReader")
    open fun defaultJsonLocalizationMapReader(context: BContext): LocalizationMapReader {
        return JacksonLocalizationMapReader.createWithDefaultTemplate(
            context,
            ObjectMapper(JsonFactory())
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature()),
            folderName = "bc_localization",
            extensions = listOf("json"),
        )
    }
}