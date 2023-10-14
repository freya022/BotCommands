package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfo
import io.github.freya022.botcommands.internal.commands.NSFWStrategy
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

sealed class TextCommandInfo(
    builder: TextCommandBuilder,
    override val parentInstance: INamedCommand?
) : AbstractCommandInfo(builder) {
    val subcommands: Map<String, TextCommandInfo>

    val variations: List<TextCommandVariation> = builder.variations.map { it.build(this) }

    val aliases: List<String> = builder.aliases

    val description: String? = builder.description

    val nsfwStrategy: NSFWStrategy? = builder.nsfwStrategy
    val isOwnerRequired: Boolean = builder.ownerRequired
    val hidden: Boolean = builder.hidden

    val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription

    init {
        subcommands = buildMap(builder.subcommands.size + builder.subcommands.sumOf { it.aliases.size }) {
            builder.subcommands.forEach { subcommandBuilder ->
                val textCommandInfo = subcommandBuilder.build(this@TextCommandInfo)
                (subcommandBuilder.aliases + subcommandBuilder.name).forEach { subcommandName ->
                    this.put(subcommandName, textCommandInfo)?.let { commandInfo ->
                        throwUser("Text subcommand with path '${commandInfo.path}' already exists")
                    }
                }
            }
        }
    }
}