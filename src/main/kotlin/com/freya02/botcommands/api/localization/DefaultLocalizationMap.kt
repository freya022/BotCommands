package com.freya02.botcommands.api.localization

import java.util.*

class DefaultLocalizationMap(
    override val effectiveLocale: Locale,
    private val localizationMap: Map<String, LocalizationTemplate?>
) : LocalizationMap {
    override fun get(path: String): LocalizationTemplate? = localizationMap[path]

    override fun toString(): String {
        return "DefaultLocalizationMap(effectiveLocale=$effectiveLocale)"
    }
}
