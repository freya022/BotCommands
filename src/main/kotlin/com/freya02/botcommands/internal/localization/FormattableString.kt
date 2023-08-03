package com.freya02.botcommands.internal.localization

interface FormattableString : LocalizableString {
    val formatterName: String
    fun format(obj: Any): String
}