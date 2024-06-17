package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

internal sealed class TextCommandInfoImpl(
    builder: TextCommandBuilder,
    override val parentInstance: TextCommandInfoImpl?
) : AbstractCommandInfoImpl(builder),
    TextCommandInfo {

    final override val subcommands: Map<String, TextCommandInfoImpl>

    final override val variations: List<TextCommandVariationImpl> = builder.variations.map { it.build(this) }.unmodifiableView()

    final override val aliases: List<String> = builder.aliases.toImmutableList()

    final override val description: String? = builder.description

    final override val nsfw: Boolean = builder.nsfw
    final override val isOwnerRequired: Boolean = builder.ownerRequired
    final override val hidden: Boolean = builder.hidden

    final override val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription

    init {
        subcommands = buildMap(builder.subcommands.size + builder.subcommands.sumOf { it.aliases.size }) {
            builder.subcommands.forEach { subcommandBuilder ->
                val textCommandInfo = subcommandBuilder.build(this@TextCommandInfoImpl)
                (subcommandBuilder.aliases + subcommandBuilder.name).forEach { subcommandName ->
                    putIfAbsentOrThrow(subcommandName, textCommandInfo) {
                        "Text subcommand with path '${it.path}' already exists"
                    }
                }
            }
        }
    }
}