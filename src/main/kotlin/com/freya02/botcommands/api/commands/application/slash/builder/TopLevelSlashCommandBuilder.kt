package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelSlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.TopLevelSlashCommandBuilderMixin
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo

class TopLevelSlashCommandBuilder internal constructor(
    private val context: BContextImpl,
    name: String,
    scope: CommandScope
) : ApplicationCommandBuilder(name), ITopLevelSlashCommandBuilder by TopLevelSlashCommandBuilderMixin(scope) {
    var description: String = DEFAULT_DESCRIPTION

    @get:JvmSynthetic
    internal val subcommands: MutableList<SlashCommandBuilder> = mutableListOf()
    @get:JvmSynthetic
    internal val subcommandGroups: MutableList<SlashSubcommandGroupBuilder> = mutableListOf()

    fun subcommand(name: String, block: SlashCommandBuilder.() -> Unit) {
        subcommands += SlashCommandBuilder(context, name).apply(block)
    }

    fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit) {
        subcommandGroups += SlashSubcommandGroupBuilder(context, name).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = SlashCommandOptionBuilder(context, declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        optionBuilders[declaredName] = ApplicationGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    internal fun build(): SlashCommandInfo {
        checkFunction()
        return SlashCommandInfo(context, this)
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"
    }
}
