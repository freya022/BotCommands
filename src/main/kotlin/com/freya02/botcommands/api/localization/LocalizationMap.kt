package com.freya02.botcommands.api.localization

import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider
import org.jetbrains.annotations.UnmodifiableView
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
     * Returns an unmodifiable set of keys this localization map contains,
     * or `null` if unsupported.
     */
    val keys: @UnmodifiableView Set<String>?

    /**
     * Returns the [LocalizationTemplate] with the corresponding path,
     * or `null` if there is no such entry.
     */
    operator fun get(path: String): LocalizationTemplate?
}

fun createDelegated(current: LocalizationMap?, parent: LocalizationMap): LocalizationMap {
    if (current == null) return parent

    return object : LocalizationMap {
        override val effectiveLocale: Locale
            get() = current.effectiveLocale

        override val keys: Set<String>?
            get() = current.keys

        override fun get(path: String): LocalizationTemplate? =
            current[path] ?: parent[path]
    }
}