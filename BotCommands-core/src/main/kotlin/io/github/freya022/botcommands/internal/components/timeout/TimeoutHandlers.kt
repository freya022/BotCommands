package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.annotations.getEffectiveName
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter

@BService
@RequiresComponents
internal class TimeoutHandlers(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) {

    private val components: Map<String, TimeoutDescriptor<ComponentTimeoutData>> =
        functionAnnotationsMap.get<ComponentTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ComponentTimeoutData::class))
            .associate {
                val function = it.function
                val annotation = function.findAnnotationRecursive<ComponentTimeoutHandler>()!!
                annotation.getEffectiveName(function) to TimeoutDescriptor(context, it.toMemberParamFunction<ComponentTimeoutData>(), ComponentTimeoutData::class)
            }

    private val groups: Map<String, TimeoutDescriptor<GroupTimeoutData>> =
        functionAnnotationsMap.get<GroupTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GroupTimeoutData::class))
            .associate {
                val function = it.function
                val annotation = function.findAnnotationRecursive<GroupTimeoutHandler>()!!
                annotation.getEffectiveName(function) to TimeoutDescriptor(context, it.toMemberParamFunction<GroupTimeoutData>(), GroupTimeoutData::class)
            }

    internal fun ofComponent(handlerName: String): TimeoutDescriptor<ComponentTimeoutData>? {
        return components[handlerName]
    }

    internal  fun ofGroup(handlerName: String): TimeoutDescriptor<GroupTimeoutData>? {
        return groups[handlerName]
    }
}
