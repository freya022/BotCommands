package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.throwUser

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String
) : ApplicationCommandBuilder(name) {
    var description: String = DEFAULT_DESCRIPTION

    protected abstract val allowOptions: Boolean
    protected abstract val allowSubcommands: Boolean
    protected abstract val allowSubcommandGroups: Boolean

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        optionBuilders[declaredName] = SlashCommandOptionBuilder(context, declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        optionBuilders[declaredName] = ApplicationGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"
    }
}
