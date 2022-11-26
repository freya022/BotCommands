package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure

@Deprecated("To be removed")
internal class DataStoreTimeoutHandler(private val classPathFunction: ClassPathFunction, private val serviceContainer: ServiceContainer) {
    private val function = classPathFunction.function

    internal suspend fun execute(dataEntity: DataEntity) {
        val args: MutableMap<KParameter, Any?> = mutableMapOf()
        args[function.instanceParameter!!] = classPathFunction.instance
        args[function.nonInstanceParameters.first()] = dataEntity

        args += function.nonInstanceParameters
            .drop(1)
            .associateWith { serviceContainer.getService(it.type.jvmErasure) }

        function.callSuspendBy(args)
    }
}
