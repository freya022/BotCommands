package com.freya02.botcommands.api.localization.arguments.factories

import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.localization.arguments.FormattableArgument
import java.util.*

@InterfacedService(acceptMultiple = true)
interface FormattableArgumentFactory {
    val regex: Regex

    fun get(matchResult: MatchResult, locale: Locale): FormattableArgument
}