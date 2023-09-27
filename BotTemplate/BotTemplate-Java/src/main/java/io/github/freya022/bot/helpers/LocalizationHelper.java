package io.github.freya022.bot.helpers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationService;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.context.LocalizationContext;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

@BService
public class LocalizationHelper {
    private final BContext context;

    public LocalizationHelper(BContext context) {
        this.context = context;
    }

    public String localize(long time, TimeUnit unit, LocalizationContext localizationContext) {
        //TODO update withBundle + localizeOrNull
        final Localization localization = context
                .getService(LocalizationService.class)
                .getInstance("Misc", Locale.forLanguageTag(localizationContext.getEffectiveLocale().getLocale()));
        if (localization == null)
            throw new IllegalStateException("Unable to find the 'Misc' localization file");

        final LocalizationTemplate template = localization.get("time_unit." + unit.name());
        if (template == null)
            return unit.name().toLowerCase().replaceAll("s$", "").trim();

        return template.localize(entry("time", time));
    }
}
