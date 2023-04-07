package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.internal.BContextImpl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalizationUtils {
    public static String getCommandRootLocalization(BContextImpl context, String path) {
        final Map<String, List<Locale>> localesMap = context.getApplicationCommandsContext().getBaseNameToLocalesMap();
        for (var baseName : localesMap.keySet()) {
            final Localization localization = Localization.getInstance(baseName, Locale.ROOT);
            if (localization != null) {
                final LocalizationTemplate template = localization.get(path);
                if (template != null) {
                    return template.localize();
                }
            }
        }

        return null;
    }
}
