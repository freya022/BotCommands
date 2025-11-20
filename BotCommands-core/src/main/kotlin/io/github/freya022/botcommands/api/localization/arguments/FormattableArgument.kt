package io.github.freya022.botcommands.api.localization.arguments

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.internal.localization.LocalizableArgument

/**
 * A formattable argument from a [LocalizationTemplate].
 */
interface FormattableArgument : LocalizableArgument {

    /**
     * Name of the argument, a [Localization.Entry.argumentName] must match this.
     */
    val argumentName: String

    /**
     * Formats the [Localization.Entry.value] into a string.
     */
    fun format(obj: Any): String
}
