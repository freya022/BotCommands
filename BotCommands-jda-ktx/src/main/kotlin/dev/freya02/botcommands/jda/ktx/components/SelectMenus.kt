package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

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

fun StringSelectMenu.Builder.option(
    label: String,
    value: String,
    description: String? = null,
    emoji: Emoji? = null,
    default: Boolean = false,
) = addOptions(SelectOption(label, value, description, emoji, default))
