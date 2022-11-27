package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.new_components.GroupTimeoutData
import com.freya02.botcommands.api.new_components.annotations.GroupTimeoutHandler
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.core.withFirstArg
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class GroupTimeoutHandlers(classPathContainer: ClassPathContainer) : HandlerContainer {
    private val map: Map<String, KFunction<*>>

    init {
        map = classPathContainer.functionsWithAnnotation<GroupTimeoutHandler>()
            .requireNonStatic()
            .withFirstArg(GroupTimeoutData::class)
            .associate {
                it.function.findAnnotation<GroupTimeoutHandler>()!!.name to it.function
            }
    }

    override operator fun get(handlerName: String) = map[handlerName]
}