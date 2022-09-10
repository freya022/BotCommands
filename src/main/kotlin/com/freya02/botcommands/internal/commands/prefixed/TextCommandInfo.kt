package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

//TODO Separate "method" into it's own interface
// method will be implemented into ApplicationCommandInfo
// Separate method from DSL too, use a "variant" function to add a text command variant for the current path
// In the end, one path corresponds to a command containing it's own variants, all the variants sharing the same description/permissions/etc...
// It's up to the user to create subcommands if they want different attributes
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

    val order: Int = builder.order //TODO remove, order must be explicit with DSL, as "order" was a workaround for java methods being unordered

    val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription
}