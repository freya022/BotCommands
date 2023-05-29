package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.GroupTimeoutHandler
import com.freya02.botcommands.api.components.data.GroupTimeoutData
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.reflection.MemberFunction
import com.freya02.botcommands.internal.core.reflection.toMemberFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.full.findAnnotation

@ConditionalService(dependencies = [Components::class])
internal class GroupTimeoutHandlers(classPathContainer: ClassPathContainer) : HandlerContainer {
    private val map: Map<String, MemberFunction<*>> =
        classPathContainer.functionsWithAnnotation<GroupTimeoutHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GroupTimeoutData::class))
            .associate {
                it.function.findAnnotation<GroupTimeoutHandler>()!!.name to it.toMemberFunction()
            }

    override operator fun get(handlerName: String) = map[handlerName]
}