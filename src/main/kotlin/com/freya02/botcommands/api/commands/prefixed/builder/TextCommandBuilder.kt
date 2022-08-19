package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.builder.CustomOptionBuilder
import com.freya02.botcommands.api.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import java.util.function.Consumer

class TextCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath) : CommandBuilder(path) {
    var aliases: MutableList<CommandPath> = arrayListOf()

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

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = TextOptionBuilder(declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        optionBuilders[declaredName] = TextGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    @JvmSynthetic
    internal fun build(): TextCommandInfo {
        checkFunction()
        return TextCommandInfo(context, this)
    }

    companion object {
        const val defaultDescription = "No description"
    }
}
