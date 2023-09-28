package io.github.freya022.bot.helpers;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.localization.context.LocalizationContext;

import java.util.concurrent.TimeUnit;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

@BService
public class LocalizationHelper {
    public String localize(long time, TimeUnit unit, LocalizationContext localizationContext) {
        final String localized = localizationContext
                .switchBundle("Misc")
                .localizeOrNull("time_unit." + unit.name(), entry("time", time));
        if (localized == null)
            return unit.name().toLowerCase().replaceAll("s$", "").trim();

        return localized;
    }
}
