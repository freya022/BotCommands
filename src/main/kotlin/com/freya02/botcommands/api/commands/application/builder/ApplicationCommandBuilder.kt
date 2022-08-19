package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.application.CommandPath
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier

abstract class ApplicationCommandBuilder internal constructor(path: CommandPath, internal val scope: CommandScope) : CommandBuilder(path) {
    var defaultLocked = false
    var testOnly = false

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    abstract fun customOption(declaredName: String)

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}
