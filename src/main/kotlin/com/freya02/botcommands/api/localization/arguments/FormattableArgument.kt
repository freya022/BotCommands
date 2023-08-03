package com.freya02.botcommands.api.localization.arguments

import com.freya02.botcommands.internal.localization.LocalizableArgument

interface FormattableArgument : LocalizableArgument {
    val formatterName: String
    fun format(obj: Any): String
}