package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationMap

internal class LocalizationImpl internal constructor(private val bundle: LocalizationMap) : Localization, LocalizationMap by bundle
