package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.core.reflection.MemberFunction
import io.github.freya022.botcommands.internal.core.reflection.toMemberFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.full.findAnnotation

@BService
@Dependencies(Components::class)
internal class ComponentTimeoutHandlers(functionAnnotationsMap: FunctionAnnotationsMap) : HandlerContainer {
    private val map: Map<String, MemberFunction<*>> =
        functionAnnotationsMap.getFunctionsWithAnnotation<ComponentTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ComponentTimeoutData::class))
            .associate {
                it.function.findAnnotation<ComponentTimeoutHandler>()!!.name to it.toMemberFunction()
            }

    override operator fun get(handlerName: String) = map[handlerName]
}