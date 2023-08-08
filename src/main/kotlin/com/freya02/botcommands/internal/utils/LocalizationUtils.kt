package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.internal.BContextImpl
import java.util.*

internal object LocalizationUtils {
    internal fun getCommandRootLocalization(context: BContextImpl, path: String): String? {
        val localesMap: Map<String, List<Locale>> = context.applicationConfig.baseNameToLocalesMap
        val localizationService = context.getService<LocalizationService>()
        for (baseName in localesMap.keys) {
            val localization = localizationService.getInstance(baseName, Locale.ROOT)
            if (localization != null) {
                val template = localization[path]
                if (template != null) {
                    return template.localize()
                }
            }
        }

        return null
    }
}