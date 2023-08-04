package com.freya02.botcommands.api.localization.arguments

import java.util.*

internal class JavaFormattableArgument internal constructor(
    override val argumentName: String,
    private val formatter: String,
    private val locale: Locale
) : FormattableArgument {
    override fun format(obj: Any): String = formatter.format(locale, obj)
}