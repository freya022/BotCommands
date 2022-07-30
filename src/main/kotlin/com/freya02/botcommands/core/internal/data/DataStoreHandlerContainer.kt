package com.freya02.botcommands.core.internal.data

import com.freya02.botcommands.core.api.annotations.ConditionalService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.core.internal.requireFirstArg
import com.freya02.botcommands.core.internal.requireNonStatic
import com.freya02.botcommands.internal.throwInternal
import kotlin.reflect.full.findAnnotation
import com.freya02.botcommands.core.internal.data.annotations.DataStoreTimeoutHandler as DataStoreTimeoutHandlerAnnotation

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