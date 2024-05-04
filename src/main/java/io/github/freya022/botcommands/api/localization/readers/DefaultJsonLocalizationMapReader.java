package io.github.freya022.botcommands.api.localization.readers;

import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper;
import io.github.freya022.botcommands.api.localization.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Default implementation for {@link LocalizationTemplate} mappings readers.
 *
 * <p>Localization templates are loaded from the {@code /bc_localization} folder (i.e., the {@code bc_localization} in your jar's root)
 * <br>Your localization bundle must be a valid JSON file and use the {@code .json} extension.
 * <br>The localization bundle can use any name, but <b>must</b> be suffixed with the same locale formatting as {@link ResourceBundle} would use, such as {@code _fr} or {@code _en_US}.
 *
 * <p>The JSON content root must be an object, where the keys must either be delimited by dots, or by using nested objects.
 * <h3>Example</h3>
 * <pre><code>
 *     {
 *         "myCommand": {
 *             "name": "my_command_in_en_US",
 *             "description": "My command description in US english"
 *         }
 *     }
 * </code></pre>
 *
 * <p>This reader uses the default localization templates, see {@link DefaultLocalizationTemplate} for more details.
 *
 * @see DefaultLocalizationTemplate
 */
@BService
public class DefaultJsonLocalizationMapReader implements LocalizationMapReader {
    private final BContext context;

    public DefaultJsonLocalizationMapReader(BContext context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LocalizationMap readLocalizationMap(@NotNull LocalizationMapRequest request) throws IOException {
        final InputStream stream = DefaultJsonLocalizationMapReader.class.getResourceAsStream("/bc_localization/" + request.bundleName() + ".json");
        if (stream == null) {
            return null;
        }

        final Map<String, LocalizationTemplate> localizationMap = new HashMap<>();

        try (var ignored = stream) {
            final Map<String, ?> map = DefaultObjectMapper.readMap(stream);

            discoverEntries(localizationMap, request.baseName(), request.requestedLocale(), "", map.entrySet());
        }

        return new DefaultLocalizationMap(request.requestedLocale(), localizationMap);
    }

    @SuppressWarnings("unchecked")
    private void discoverEntries(Map<String, LocalizationTemplate> localizationMap, @NotNull String baseName, Locale effectiveLocale, String currentPath, Set<? extends Map.Entry<String, ?>> entries) {
        for (Map.Entry<String, ?> entry : entries) {
            final String key = appendPath(currentPath, entry.getKey());

            if (entry.getValue() instanceof Map<?, ?> map) {
                discoverEntries(
                        localizationMap,
                        baseName,
                        effectiveLocale,
                        key,
                        ((Map<String, ?>) map).entrySet()
                );
            } else {
                if (!(entry.getValue() instanceof String))
                    throw new IllegalArgumentException("Key '%s' in bundle '%s' (locale '%s') can only be a String or a Map (JSON Object), found: %s".formatted(key, baseName, effectiveLocale, entry.getValue().getClass().getSimpleName()));

                final LocalizationTemplate value = new DefaultLocalizationTemplate(context, (String) entry.getValue(), effectiveLocale);

                if (localizationMap.put(key, value) != null) {
                    throw new IllegalStateException("Got two same localization keys: '" + key + "'");
                }
            }
        }
    }
}
