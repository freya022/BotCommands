package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.LocalizationMap
import com.freya02.botcommands.api.localization.LocalizationTemplate
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

internal class LocalizationImpl internal constructor(bundle: LocalizationMap) : Localization {
    private val templateMap: Map<String, LocalizationTemplate> = bundle.templateMap()
    private val effectiveLocale: Locale = bundle.effectiveLocale()

    override fun getTemplateMap(): @UnmodifiableView Map<String, LocalizationTemplate> {
        return Collections.unmodifiableMap(templateMap)
    }

    override fun get(path: String): LocalizationTemplate? = templateMap[path]

    override fun getEffectiveLocale(): Locale = effectiveLocale
}
