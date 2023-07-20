package com.freya02.botcommands.api.localization.readers;

import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.TemplateMapRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public interface LocalizationMapReader {
    /**
     * Utility method to append a path component to an existing path, this is simply {@code path + '.' + other}.
     *
     * @param path  The current path
     * @param other The other path component
     *
     * @return The new path
     */
    default String appendPath(String path, String other) {
        if (path.isBlank()) return other;

        return path + '.' + other;
    }

    @Nullable
    Map<String, LocalizationTemplate> readTemplateMap(@NotNull TemplateMapRequest request) throws IOException;
}
