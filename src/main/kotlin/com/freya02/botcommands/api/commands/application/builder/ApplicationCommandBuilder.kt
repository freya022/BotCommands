package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.BuilderFunctionHolder
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder

abstract class ApplicationCommandBuilder internal constructor(
    name: String
) : CommandBuilder(name), IBuilderFunctionHolder<Any> by BuilderFunctionHolder() {
    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    var defaultLocked: Boolean = DEFAULT_DEFAULT_LOCKED

    var nsfw: Boolean = false

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    abstract fun customOption(declaredName: String)

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)

    companion object {
        const val DEFAULT_DEFAULT_LOCKED = false
    }
}
