package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

interface TextCommandInfo : CommandInfo {
    val subcommands: Map<String, TextCommandInfo>

    val variations: List<TextCommandVariation>

    val aliases: List<String>

    val description: String?

    val nsfw: Boolean
    val isOwnerRequired: Boolean
    val hidden: Boolean

    val detailedDescription: Consumer<EmbedBuilder>?
}