package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.emoji.Emoji

@ReplaceJdaKtx
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

@ReplaceJdaKtx("dev.minn.jda.ktx.interactions.components")
fun StringSelectMenu.Builder.option(
    label: String,
    value: String,
    description: String? = null,
    emoji: Emoji? = null,
    default: Boolean = false,
) = addOptions(SelectOption(label, value, description, emoji, default))
