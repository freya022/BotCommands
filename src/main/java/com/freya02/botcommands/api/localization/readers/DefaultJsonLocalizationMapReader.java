package com.freya02.botcommands.api.localization.readers;

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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@BService
@ServiceType(types = LocalizationMapReader.class)
public class DefaultJsonLocalizationMapReader implements LocalizationMapReader {
    private static final Gson GSON = new Gson();

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

                final LocalizationTemplate value = new DefaultLocalizationTemplate((String) entry.getValue(), effectiveLocale);

                if (templateMap.put(key, value) != null) {
                    throw new IllegalStateException("Got two same localization keys: '" + key + "'");
                }
            }
        }
    }

    private static class TemplateMapTypeToken extends TypeToken<Map<String, ?>> {
    }
}
