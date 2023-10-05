package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.ComponentDSL
import io.github.freya022.botcommands.internal.components.PersistentHandler

@ComponentDSL
class PersistentHandlerBuilder internal constructor(val handlerName: String) {
    private var data: List<Any?> = emptyList()

    /**
     * Sets the data passed to the persistent handler's function.
     */
    fun passData(data: List<Any?>) {
        this.data = data
    }

    /**
     * Sets the data passed to the persistent handler's function.
     */
    fun passData(vararg data: Any?): Unit = passData(data.asList())

    @JvmSynthetic
    internal fun build(): PersistentHandler = PersistentHandler(handlerName, data)
}