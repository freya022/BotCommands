package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.builder.CommandBuilder

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
