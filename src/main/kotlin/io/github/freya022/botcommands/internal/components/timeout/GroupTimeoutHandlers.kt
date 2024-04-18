package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.full.findAnnotation

@BService
@RequiresComponents
internal class GroupTimeoutHandlers(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) : TimeoutHandlerContainer {
    private val map: Map<String, TimeoutDescriptor<GroupTimeoutData>> =
        functionAnnotationsMap.get<GroupTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GroupTimeoutData::class))
            .associate {
                it.function.findAnnotation<GroupTimeoutHandler>()!!.name to TimeoutDescriptor(context, it.toMemberParamFunction(), GroupTimeoutData::class)
            }

    override operator fun get(handlerName: String) = map[handlerName]
}