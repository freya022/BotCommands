package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.components.ComponentDSL
import com.freya02.botcommands.internal.components.PersistentHandler

@ComponentDSL
class PersistentHandlerBuilder internal constructor(val handlerName: String) {
    private var data: List<Any?> = emptyList()

    fun passData(data: List<Any?>) {
        this.data = data
    }

    fun passData(vararg data: Any?): Unit = passData(data.asList())

    @JvmSynthetic
    internal fun build(): PersistentHandler = PersistentHandler(handlerName, data)
}