package doc.java.examples.localization.readers;

import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.localization.readers.JsonLocalizationMapReader;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import io.github.freya022.botcommands.test.switches.TestService;

@TestService
@TestLanguage(TestLanguage.Language.JAVA)
@BConfiguration
public class LocalizationMapReaderProvider {

    @BService // Creates a new LocalizationMapReader which finds its JSON files in the "doxxy" folder
    public static LocalizationMapReader doxxyLocalizationReader(BContext context) {
        return new JsonLocalizationMapReader(context, "doxxy");
    }
}