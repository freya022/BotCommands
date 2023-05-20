package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.ExecutableCommandBuilder
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilder<T, Any>(name, function) {
    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    var defaultLocked: Boolean = DEFAULT_DEFAULT_LOCKED

    var nsfw: Boolean = false

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    companion object {
        const val DEFAULT_DEFAULT_LOCKED = false
    }
}
