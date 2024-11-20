package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.ReceiverConsumer
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

    companion object {
        @JvmStatic
        @JvmName("create")
        operator fun invoke(
            request: LocalizationMapRequest,
            builder: ReceiverConsumer<MutableMap<String, LocalizationTemplate>>
        ) = DefaultLocalizationMap(request.requestedLocale, buildMap(builder))
    }
}
