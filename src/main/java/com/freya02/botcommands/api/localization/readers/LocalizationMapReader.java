package com.freya02.botcommands.api.localization.readers;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.TemplateMapRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * Reads localization mappings
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService},
 * and a {@link ServiceType} of {@link LocalizationMapReader}.
 */
@InterfacedService(acceptMultiple = true)
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
