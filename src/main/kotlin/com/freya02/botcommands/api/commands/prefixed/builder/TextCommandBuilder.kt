package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.builder.NSFWStrategyBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.NSFWStrategy
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

abstract class TextCommandBuilder internal constructor(protected val context: BContextImpl, name: String) : CommandBuilder(name) {
    @get:JvmSynthetic
    internal val subcommands: MutableList<TextSubcommandBuilder> = arrayListOf()

    @get:JvmSynthetic
    internal val variations: MutableList<TextCommandVariationBuilder> = arrayListOf()

    @get:JvmSynthetic
    internal var nsfwStrategy: NSFWStrategy? = null
        private set

    var aliases: MutableList<String> = arrayListOf()

    var description = defaultDescription

    var ownerRequired: Boolean = false
    var hidden: Boolean = false

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
        subcommands += TextSubcommandBuilder(context, name).apply(block)
    }

    fun variation(block: TextCommandVariationBuilder.() -> Unit) {
        variations += TextCommandVariationBuilder(context).apply(block)
    }

    fun nsfw(block: NSFWStrategyBuilder.() -> Unit) {
        nsfwStrategy = NSFWStrategyBuilder().apply(block).build()
    }

    companion object {
        const val defaultDescription = "No description"
    }
}
