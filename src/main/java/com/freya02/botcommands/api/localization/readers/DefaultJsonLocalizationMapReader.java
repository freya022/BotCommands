package com.freya02.botcommands.api.localization.readers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;
import com.freya02.botcommands.api.localization.DefaultLocalizationTemplate;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.TemplateMapRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
@ServiceType(types = LocalizationMapReader.class)
public class DefaultJsonLocalizationMapReader implements LocalizationMapReader {
    private static final Gson GSON = new Gson();

    private final BContext context;

    public DefaultJsonLocalizationMapReader(BContext context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Map<String, LocalizationTemplate> readTemplateMap(@NotNull TemplateMapRequest request) throws IOException {
        final InputStream stream = DefaultJsonLocalizationMapReader.class.getResourceAsStream("/bc_localization/" + request.bundleName() + ".json");
        if (stream == null) {
            return null;
        }

        final Map<String, LocalizationTemplate> templateMap = new HashMap<>();

        try (InputStreamReader reader = new InputStreamReader(stream)) {
            final Map<String, ?> map = GSON.fromJson(reader, new TemplateMapTypeToken());

            discoverEntries(templateMap, request.baseName(), request.effectiveLocale(), "", map.entrySet());
        }

        return templateMap;
    }

    @SuppressWarnings("unchecked")
    private void discoverEntries(Map<String, LocalizationTemplate> templateMap, @NotNull String baseName, Locale effectiveLocale, String currentPath, Set<? extends Map.Entry<String, ?>> entries) {
        for (Map.Entry<String, ?> entry : entries) {
            final String key = appendPath(currentPath, entry.getKey());

            if (entry.getValue() instanceof Map<?, ?> map) {
                discoverEntries(
                        templateMap,
                        baseName,
                        effectiveLocale,
                        key,
                        ((Map<String, ?>) map).entrySet()
                );
            } else {
                if (!(entry.getValue() instanceof String))
                    throw new IllegalArgumentException("Key '%s' in bundle '%s' (locale '%s') can only be a String".formatted(key, baseName, effectiveLocale));

                final LocalizationTemplate value = new DefaultLocalizationTemplate(context, (String) entry.getValue(), effectiveLocale);

                if (templateMap.put(key, value) != null) {
                    throw new IllegalStateException("Got two same localization keys: '" + key + "'");
                }
            }
        }
    }

    private static class TemplateMapTypeToken extends TypeToken<Map<String, ?>> {
    }
}
