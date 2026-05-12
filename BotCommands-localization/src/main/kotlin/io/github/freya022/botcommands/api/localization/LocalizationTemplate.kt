package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.core.utils.mapToArray
import io.github.freya022.botcommands.api.localization.arguments.FormattableArgument

/**
 * Represents an entire localizable string, with parameters.
 *
 * You can implement your own template, the default one is [DefaultLocalizationTemplate].
 *
 * @see DefaultLocalizationTemplate
 */
interface LocalizationTemplate {

    /**
     * Details about the arguments a user needs to supply to [localize].
     */
    val arguments: List<FormattableArgument>

    /**
     * Processes the localization template and replaces the named parameters by their values
     */
    fun localize(vararg args: Localization.Entry): String
}

/**
 * Processes the localization template and replaces the named parameters by their values
 */
fun LocalizationTemplate.localize(vararg args: Pair<String, Any>): String =
    localize(*args.mapToArray { (k, v) -> Localization.Entry.entry(k, v) })
