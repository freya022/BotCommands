package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier

abstract class ApplicationCommandBuilder internal constructor(
    name: String
) : CommandBuilder(name) {
    var defaultLocked = DEFAULT_DEFAULT_LOCKED
    var testOnly = false

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
