package io.github.freya022.botcommands.api.localization.arguments

import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.internal.localization.LocalizableArgument

/**
 * A formattable argument from a [LocalizationTemplate].
 */
interface FormattableArgument : LocalizableArgument {
    val argumentName: String
    fun format(obj: Any): String
}