package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.LocalizationMap

internal class LocalizationImpl internal constructor(private val bundle: LocalizationMap) : Localization, LocalizationMap by bundle
