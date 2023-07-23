package com.freya02.botcommands.api.localization.readers;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.service.ServiceContainer;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.TemplateMapRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;

/**
 * Class which contains all the {@link LocalizationMapReader}.
 * <br>This is mainly used by the localization map providers.
 */
@BService
public final class LocalizationMapReaders {
    private static final Logger LOGGER = Logging.getLogger();
    private static final ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    private final ServiceContainer serviceContainer;

    private Collection<LocalizationMapReader> readers = null;

    public LocalizationMapReaders(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

	@NotNull
	@UnmodifiableView
    public Collection<LocalizationMapReader> getReaders() {
        if (readers == null)
            readers = serviceContainer.getInterfacedServices(LocalizationMapReader.class);
        return Collections.unmodifiableCollection(readers);
    }

    /**
     * Cycles through all the registered readers with the specified base name and locale,
     * and returns when a reader returns non-null mappings, returns null otherwise
     *
     * @param baseName The base name of the localization bundle
     * @param locale   The locale of the localization bundle
     *
     * @return non-null mappings if a reader returned one, {@code null} otherwise
     */
    @Nullable
    public Map<String, LocalizationTemplate> cycleReaders(@NotNull String baseName, @NotNull Locale locale) {
        final TemplateMapRequest request = new TemplateMapRequest(baseName, locale, CONTROL.toBundleName(baseName, locale));
        for (LocalizationMapReader reader : getReaders()) {
            try {
                Map<String, LocalizationTemplate> templateMap = reader.readTemplateMap(request);

                if (templateMap != null) {
                    return templateMap;
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading a bundle '{}' with locale '{}' with reader '{}'", baseName, locale, reader.getClass().getName());
            }
        }

        return null;
    }
}
