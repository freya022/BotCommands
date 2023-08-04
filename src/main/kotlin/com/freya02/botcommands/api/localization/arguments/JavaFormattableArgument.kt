package com.freya02.botcommands.api.localization.arguments

import java.util.*

class JavaFormattableArgument(
    override val argumentName: String,
    private val formatter: String,
    private val locale: Locale
) : FormattableArgument {
    override fun format(obj: Any): String = formatter.format(locale, obj)

    override fun toString(): String {
        return "JavaFormattableArgument(argumentName='$argumentName', formatter='$formatter', locale=$locale)"
    }
}