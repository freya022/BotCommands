package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

class TextCommandInfo(
    context: BContextImpl,
    builder: TextCommandBuilder,
    override val parentInstance: INamedCommandInfo?
) : AbstractCommandInfo(context, builder) {
    val subcommands: Map<String, TextCommandInfo> = builder.subcommands.associate { it.name to it.build(this) }

    val variations: List<TextCommandVariation> = builder.variations.map { it.build(this) }

    val aliases: List<String> = builder.aliases

    val category: String = builder.category
    val description: String = builder.description

    val isOwnerRequired: Boolean = builder.ownerRequired
    val hidden: Boolean = builder.hidden

    val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription
}