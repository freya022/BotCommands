package com.freya02.botcommands.internal.data

import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.throwInternal
import kotlin.reflect.full.findAnnotation
import com.freya02.botcommands.internal.data.annotations.DataStoreTimeoutHandler as DataStoreTimeoutHandlerAnnotation

@ConditionalService
internal class DataStoreHandlerContainer(classPathContainer: ClassPathContainer, serviceContainer: ServiceContainer) {
    val timeoutHandlers: Map<String, DataStoreTimeoutHandler>

    init {
        timeoutHandlers = classPathContainer.functionsWithAnnotation<DataStoreTimeoutHandlerAnnotation>()
            .requireNonStatic()
            .requireFirstArg(DataEntity::class)
            .associate {
                val annotation = it.function.findAnnotation<DataStoreTimeoutHandlerAnnotation>() ?: throwInternal("DataStoreTimeoutHandlerAnnotation is null")

                annotation.name to DataStoreTimeoutHandler(it, serviceContainer)
            }
    }
}