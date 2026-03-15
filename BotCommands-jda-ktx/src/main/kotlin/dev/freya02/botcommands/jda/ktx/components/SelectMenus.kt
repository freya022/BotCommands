package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * Creates a select menu option, see [SelectOption].
 *
 * @param label       The label of this option, see [SelectOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [SelectOption.withValue]
 * @param description The description of this option, see [SelectOption.withDescription]
 * @param emoji       The emoji of this option
 * @param default     Whether this option is selected by default
 */
fun SelectOption(
    label: String,
    value: String,
    description: String? = null,
    emoji: Emoji? = null,
    default: Boolean = false,
) = SelectOption.of(label, value)
    .withDescription(description)
    .withEmoji(emoji)
    .withDefault(default)

/**
 * Adds an option to this select menu, see [SelectOption].
 *
 * @param label       The label of this option, see [SelectOption.withLabel]
 * @param value       The value of this option, this is what the bot receives, see [SelectOption.withValue]
 * @param description The description of this option, see [SelectOption.withDescription]
 * @param emoji       The emoji of this option
 * @param default     Whether this option is selected by default
 */
fun StringSelectMenu.Builder.option(
    label: String,
    value: String,
    description: String? = null,
    emoji: Emoji? = null,
    default: Boolean = false,
) = addOptions(SelectOption(label, value, description, emoji, default))
