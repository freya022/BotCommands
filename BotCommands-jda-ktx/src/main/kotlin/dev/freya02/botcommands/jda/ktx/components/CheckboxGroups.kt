package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroupOption

/**
 * Creates a [CheckboxGroupOption][net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroupOption].
 *
 * @param label       The label of this option, see [CheckboxGroupOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [CheckboxGroupOption.withValue]
 * @param description The description of this option, see [CheckboxGroupOption.withDescription]
 * @param default     Whether this option is selected by default
 */
fun CheckboxGroupOption(
    label: String,
    value: String,
    description: String? = null,
    default: Boolean = false,
) = CheckboxGroupOption.of(label, value, description, default)

/**
 * Adds an option to this select menu, see [CheckboxGroupOption].
 *
 * @param label       The label of this option, see [CheckboxGroupOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [CheckboxGroupOption.withValue]
 * @param description The description of this option, see [CheckboxGroupOption.withDescription]
 * @param default     Whether this option is selected by default
 */
fun CheckboxGroup.Builder.option(
    label: String,
    value: String,
    description: String? = null,
    default: Boolean = false,
) = addOptions(CheckboxGroupOption(label, value, description, default))
