package com.freya02.botcommands.api.localization

/**
 * Allows different implementation of localization templates.
 *
 * A localization template represents the entire string being localized, with parameters.
 *
 * You can implement your own template, the default one is [DefaultLocalizationTemplate].
 *
 * @see DefaultLocalizationTemplate
 */
interface LocalizationTemplate {
    /**
     * Processes the localization template and replaces the named parameters by theirs values
     */
    fun localize(vararg args: Localization.Entry): String
}

/**
 * Processes the localization template and replaces the named parameters by theirs values
 */
fun LocalizationTemplate.localize(vararg args: Pair<String, Any>): String =
    localize(*args.map { (k, v) -> Localization.Entry.entry(k, v) }.toTypedArray())