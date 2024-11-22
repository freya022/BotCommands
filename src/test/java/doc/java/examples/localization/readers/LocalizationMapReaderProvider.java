package doc.java.examples.localization.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import io.github.freya022.botcommands.test.switches.TestService;

import java.util.List;

@TestService
@TestLanguage(TestLanguage.Language.JAVA)
@BConfiguration
public class LocalizationMapReaderProvider {

    @TestService
    @TestLanguage(TestLanguage.Language.JAVA) //TODO this should be redundant by the class annotation
    @BService // Creates a new LocalizationMapReader which finds its YAML files in the "doxxy" folder
    public static LocalizationMapReader doxxyLocalizationReader(BContext context) {
        return JacksonLocalizationMapReader.createWithDefaultTemplate(
                context,
                new ObjectMapper(new YAMLFactory()),
                /* folderName */ "doxxy",
                /* extensions */ List.of("yaml", "yml")
        );
    }
}