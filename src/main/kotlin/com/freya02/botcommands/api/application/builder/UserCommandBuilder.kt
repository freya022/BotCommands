package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo

class UserCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath, scope: CommandScope) :
    ApplicationCommandBuilder(path, scope) {

    override val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    /**
     * @param name Name of the declared parameter in the [function]
     */
    fun option(name: String) {
        optionBuilders[name] = UserCommandOptionBuilder(name)
    }

    /**
     * @param name Name of the declared parameter in the [function]
     */
    fun customOption(name: String) {
        optionBuilders[name] = CustomOptionBuilder(name)
    }

    internal fun build(): UserCommandInfo {
        return UserCommandInfo(context, this)
    }
}
