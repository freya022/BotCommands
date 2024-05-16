package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.components.ComponentDSL
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import net.dv8tion.jda.api.entities.ISnowflake

@ComponentDSL
class PersistentHandlerBuilder internal constructor(val handlerName: String) {
    private var data: List<Any?> = emptyList()

    /**
     * Sets the data passed to the persistent handler's function.
     *
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
     *
     * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
     */
    fun passData(data: List<Any?>) {
        this.data = data
    }

    /**
     * Sets the data passed to the persistent handler's function.
     *
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
     *
     * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
     */
    fun passData(vararg data: Any?): Unit = passData(data.asList())

    @JvmSynthetic
    internal fun build(): PersistentHandler = PersistentHandler.create(handlerName, data)
}