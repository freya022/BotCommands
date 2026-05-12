package doc.java.examples.localization.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest;
import io.github.freya022.botcommands.api.localization.readers.AbstractJacksonLocalizationMapReader;
import io.github.freya022.botcommands.api.localization.readers.LocalizationTemplateFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;

@NullMarked
public class YamlLocalizationMapReader extends AbstractJacksonLocalizationMapReader {

    private final String folderName;

    public YamlLocalizationMapReader(LocalizationTemplateFunction templateFunction, String folderName) {
        super(new ObjectMapper(new YAMLFactory()), templateFunction);
        this.folderName = folderName;
    }

    @Nullable
    @Override
    public InputStream getInputStream(LocalizationMapRequest request) {
        final InputStream stream = getClass().getResourceAsStream("/%s/%s.yml".formatted(folderName, request.bundleName()));
        if (stream != null) return stream;

        return getClass().getResourceAsStream("/%s/%s.yaml".formatted(folderName, request.bundleName()));
    }
}
