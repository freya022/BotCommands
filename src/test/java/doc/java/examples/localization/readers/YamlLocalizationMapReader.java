package doc.java.examples.localization.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest;
import io.github.freya022.botcommands.api.localization.readers.AbstractJacksonLocalizationMapReader;
import io.github.freya022.botcommands.api.localization.readers.LocalizationTemplateFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class YamlLocalizationMapReader extends AbstractJacksonLocalizationMapReader {

    private final String folderName;

    public YamlLocalizationMapReader(@NotNull LocalizationTemplateFunction templateFunction, @NotNull String folderName) {
        super(new ObjectMapper(new YAMLFactory()), templateFunction);
        this.folderName = folderName;
    }

    @Nullable
    @Override
    public InputStream getInputStream(@NotNull LocalizationMapRequest request) {
        final InputStream stream = getClass().getResourceAsStream("/%s/%s.yml".formatted(folderName, request.bundleName()));
        if (stream != null) return stream;

        return getClass().getResourceAsStream("/%s/%s.yaml".formatted(folderName, request.bundleName()));
    }
}
