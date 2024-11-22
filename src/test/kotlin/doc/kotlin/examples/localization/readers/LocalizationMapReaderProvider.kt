package doc.kotlin.examples.localization.readers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.github.freya022.botcommands.test.switches.TestLanguage
import io.github.freya022.botcommands.test.switches.TestService

@TestService
@TestLanguage(TestLanguage.Language.KOTLIN)
@BConfiguration
object LocalizationMapReaderProvider {

    @TestService
    @TestLanguage(TestLanguage.Language.KOTLIN) //TODO this should be redundant by the class annotation
    @BService // Creates a new LocalizationMapReader which finds its YAML files in the "doxxy" folder
    fun doxxyLocalizationReader(context: BContext): LocalizationMapReader {
        return JacksonLocalizationMapReader.createWithDefaultTemplate(
            context,
            ObjectMapper(YAMLFactory()),
            folderName = "doxxy",
            extensions = listOf("yaml", "yml")
        )
    }
}