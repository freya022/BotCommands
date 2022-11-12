package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.new_components.ComponentTimeoutData
import com.freya02.botcommands.api.new_components.annotations.ComponentTimeoutHandler
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.core.withFirstArg
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class ComponentTimeoutHandlers(classPathContainer: ClassPathContainer) {
    private val map: Map<String, KFunction<*>>

    init {
        map = classPathContainer.functionsWithAnnotation<ComponentTimeoutHandler>()
            .requireNonStatic()
            .withFirstArg(ComponentTimeoutData::class)
            .associate {
                it.function.findAnnotation<ComponentTimeoutHandler>()!!.name to it.function
            }
    }

    operator fun get(handlerName: String) = map[handlerName]
}