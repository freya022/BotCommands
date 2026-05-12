package doc.kotlin.examples.localization.readers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.readers.AbstractJacksonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationTemplateFunction
import java.io.InputStream

class YamlLocalizationMapReader(
    localizationTemplateFunction: LocalizationTemplateFunction,
    private val folderName: String,
) : AbstractJacksonLocalizationMapReader(
    ObjectMapper(YAMLFactory()), // Give it a YAMLFactory instead of the default JSONFactory
    localizationTemplateFunction,
) {

    override fun getInputStream(request: LocalizationMapRequest): InputStream? {
        return this.javaClass.getResourceAsStream("/$folderName/${request.bundleName}.yml")
            ?: this.javaClass.getResourceAsStream("/$folderName/${request.bundleName}.yaml")
    }
}