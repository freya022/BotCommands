package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.radiogroup.RadioGroup
import net.dv8tion.jda.api.components.radiogroup.RadioGroupOption

/**
 * Creates a [RadioGroupOption][net.dv8tion.jda.api.components.radiogroup.RadioGroupOption].
 *
 * @param label       The label of this option, see [RadioGroupOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [RadioGroupOption.withValue]
 * @param description The description of this option, see [RadioGroupOption.withDescription]
 * @param default     Whether this option is selected by default
 */
fun RadioGroupOption(
    label: String,
    value: String,
    description: String? = null,
    default: Boolean = false,
) = RadioGroupOption.of(label, value, description, default)

/**
 * Adds an option to this select menu, see [RadioGroupOption].
 *
 * @param label       The label of this option, see [RadioGroupOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [RadioGroupOption.withValue]
 * @param description The description of this option, see [RadioGroupOption.withDescription]
 * @param default     Whether this option is selected by default
 */
fun RadioGroup.Builder.option(
    label: String,
    value: String,
    description: String? = null,
    default: Boolean = false,
) = addOptions(RadioGroupOption(label, value, description, default))
