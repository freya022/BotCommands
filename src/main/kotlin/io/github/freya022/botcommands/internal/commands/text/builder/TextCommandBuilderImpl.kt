package io.github.freya022.botcommands.internal.commands.text.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.builder.CommandBuilderImpl
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.internal.utils.Checks
import java.util.function.Consumer
import kotlin.reflect.KFunction

internal abstract class TextCommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
) : CommandBuilderImpl(context, name),
    TextCommandBuilder {

    final override val type: CommandType = CommandType.TEXT
    internal val subcommands: MutableList<TextSubcommandBuilderImpl> = arrayListOf()

    internal val variations: MutableList<TextCommandVariationBuilderImpl> = arrayListOf()

    final override var nsfw: Boolean = false

    final override var aliases: MutableList<String> = arrayListOf()

    final override var description: String? = null

    final override var ownerRequired: Boolean = false

    final override var hidden: Boolean = false

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    final override var detailedDescription: Consumer<EmbedBuilder>? = null

    final override fun subcommand(name: String, block: TextCommandBuilder.() -> Unit) {
        subcommands += TextSubcommandBuilderImpl(context, name, this)
            .setCallerAsDeclarationSite()
            .apply(block)
    }

    final override fun variation(function: KFunction<Any>, block: TextCommandVariationBuilder.() -> Unit) {
        variations += TextCommandVariationBuilderImpl(context, function)
            .setCallerAsDeclarationSite()
            .apply(block)
    }
}