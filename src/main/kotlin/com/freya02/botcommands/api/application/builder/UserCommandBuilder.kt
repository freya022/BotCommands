package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo

class UserCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath, scope: CommandScope) :
    ApplicationCommandBuilder(path, scope) {

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun option(declaredName: String) {
        optionBuilders[declaredName] = UserCommandOptionBuilder(declaredName)
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

    internal fun build(): UserCommandInfo {
        checkFunction()
        return UserCommandInfo(context, this)
    }
}
