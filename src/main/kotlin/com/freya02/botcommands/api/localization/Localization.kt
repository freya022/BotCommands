package com.freya02.botcommands.api.localization

/**
 * Create a new localization entry, this binds a key (from a templated string) to the value
 *
 * @receiver    The key from the templated string
 * @param value The value to assign it to
 *
 * @see Localization
 *
 * @see localize LocalizationTemplate.localize
 */
infix fun String.to(value: Any): Localization.Entry = Localization.Entry.entry(this, value)