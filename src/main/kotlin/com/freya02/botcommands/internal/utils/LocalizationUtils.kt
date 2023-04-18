package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.internal.BContextImpl
import java.util.*

internal object LocalizationUtils {
    fun getCommandRootLocalization(context: BContextImpl, path: String?): String? {
        val localesMap: Map<String, List<Locale>> = context.applicationConfig.baseNameToLocalesMap
        for (baseName in localesMap.keys) {
            val localization = Localization.getInstance(baseName, Locale.ROOT)
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