package com.freya02.botcommands.internal.localization

interface FormattableArgument : LocalizableArgument {
    val formatterName: String
    fun format(obj: Any): String
}