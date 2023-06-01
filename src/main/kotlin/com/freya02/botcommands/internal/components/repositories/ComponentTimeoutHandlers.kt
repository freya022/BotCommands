package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.ComponentTimeoutHandler
import com.freya02.botcommands.api.components.data.ComponentTimeoutData
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.reflection.MemberFunction
import com.freya02.botcommands.internal.core.reflection.toMemberFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.full.findAnnotation

@BService
@Dependencies([Components::class])
internal class ComponentTimeoutHandlers(classPathContainer: ClassPathContainer) : HandlerContainer {
    private val map: Map<String, MemberFunction<*>> =
        classPathContainer.functionsWithAnnotation<ComponentTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ComponentTimeoutData::class))
            .associate {
                it.function.findAnnotation<ComponentTimeoutHandler>()!!.name to it.toMemberFunction()
            }

    override operator fun get(handlerName: String) = map[handlerName]
}