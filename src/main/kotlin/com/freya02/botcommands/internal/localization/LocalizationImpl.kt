package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.LocalizationMap
import com.freya02.botcommands.api.localization.LocalizationTemplate
import java.util.*

internal class LocalizationImpl internal constructor(private val bundle: LocalizationMap) : Localization {
    override fun get(path: String): LocalizationTemplate? = bundle[path]

    override fun getEffectiveLocale(): Locale = bundle.effectiveLocale
}
