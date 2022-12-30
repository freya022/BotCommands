package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.GroupTimeoutHandler
import com.freya02.botcommands.api.components.data.GroupTimeoutData
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.core.withFirstArg
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@ConditionalService(dependencies = [Components::class])
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