package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

class TextCommandBuilder internal constructor(private val context: BContextImpl, name: String) : CommandBuilder(name) {
    @get:JvmSynthetic //TODO don't use TextCommandBuilder for subcommands, subcommands don't have categories for example
    internal val subcommands: MutableList<TextCommandBuilder> = arrayListOf()

    @get:JvmSynthetic
    internal val variations: MutableList<TextCommandVariationBuilder> = arrayListOf()

    var aliases: MutableList<String> = arrayListOf()

    var category: String = "No category"
    var description = defaultDescription

    var ownerRequired: Boolean = false
    var hidden: Boolean = false

    var order = 0

    /**
     * Returns a detailed embed of what the command is, it is used by the internal <code>'help'</code> command
     *
     * The "`help`" command will automatically set the embed title to be "`Command 'command_name'`" but can be overridden
     *
     * It will also set the embed's description to be the command's description, **you can override with [EmbedBuilder.setDescription]**
     *
     * @return The EmbedBuilder to use as a detailed description
     */
    var detailedDescription: Consumer<EmbedBuilder>? = null

    fun subcommand(name: String, block: TextCommandBuilder.() -> Unit) {
        subcommands += TextCommandBuilder(context, name).apply(block)
    }

    fun variation(block: TextCommandVariationBuilder.() -> Unit) {
        variations += TextCommandVariationBuilder(context).apply(block)
    }

    @JvmSynthetic
    internal fun build(parentInstance: TextCommandInfo?): TextCommandInfo {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TextCommandInfo(context, this, parentInstance)
    }

    companion object {
        const val defaultDescription = "No description"
    }
}
