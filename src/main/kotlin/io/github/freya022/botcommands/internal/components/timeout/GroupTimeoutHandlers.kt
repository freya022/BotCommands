package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.full.findAnnotation

@BService
@Dependencies(Components::class)
internal class GroupTimeoutHandlers(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) : HandlerContainer {
    private val map: Map<String, TimeoutDescriptor<GroupTimeoutData>> =
        functionAnnotationsMap.getFunctionsWithAnnotation<GroupTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GroupTimeoutData::class))
            .associate {
                it.function.findAnnotation<GroupTimeoutHandler>()!!.name to TimeoutDescriptor(context, it.toMemberParamFunction(), GroupTimeoutData::class)
            }

    override operator fun get(handlerName: String) = map[handlerName]
}