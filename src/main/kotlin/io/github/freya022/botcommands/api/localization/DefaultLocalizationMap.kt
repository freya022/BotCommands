package io.github.freya022.botcommands.api.localization

import java.util.*

class DefaultLocalizationMap(
    override val effectiveLocale: Locale,
    private val localizationMap: Map<String, LocalizationTemplate?>
) : LocalizationMap {
    override val keys: Set<String>
        get() = Collections.unmodifiableSet(localizationMap.keys)

    override fun get(path: String): LocalizationTemplate? = localizationMap[path]

    override fun toString(): String {
        return "DefaultLocalizationMap(effectiveLocale=$effectiveLocale)"
    }
}
