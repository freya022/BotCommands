package com.freya02.botcommands.api.localization.arguments

import com.freya02.botcommands.api.localization.LocalizationTemplate
import com.freya02.botcommands.internal.localization.LocalizableArgument

/**
 * A formattable argument from a [LocalizationTemplate].
 */
interface FormattableArgument : LocalizableArgument {
    val argumentName: String
    fun format(obj: Any): String
}