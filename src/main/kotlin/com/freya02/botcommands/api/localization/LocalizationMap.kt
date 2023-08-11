package com.freya02.botcommands.api.localization

import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider
import java.util.*

/**
 * Common interface to retrieve [localization templates][LocalizationTemplate] from a path.
 */
interface LocalizationMap {
    /**
     * Returns the effective locale of this localization map.
     *
     * **Note:** this doesn't need to be the locale passed by [LocalizationMapProvider.fromBundleOrParent].
     */
    val effectiveLocale: Locale

    /**
     * Returns the [LocalizationTemplate] with the corresponding path,
     * or `null` if there is no such entry.
     */
    operator fun get(path: String): LocalizationTemplate?
}

fun createDelegated(current: LocalizationMap?, parent: LocalizationMap): LocalizationMap {
    if (current == null) return parent

    val effectiveLocale = current.effectiveLocale
    return object : LocalizationMap {
        override val effectiveLocale: Locale
            get() = effectiveLocale

        override fun get(path: String): LocalizationTemplate? {
            val template = current[path]
            if (template != null) return template
            return parent[path]
        }
    }
}