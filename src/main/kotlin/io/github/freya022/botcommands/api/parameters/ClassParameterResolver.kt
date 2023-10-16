package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlin.reflect.KClass

@InterfacedService(acceptMultiple = true)
abstract class ClassParameterResolver<T : ClassParameterResolver<T, R>, R : Any>(
    val jvmErasure: KClass<R>
) : ParameterResolver<T, R>() {
    constructor(clazz: Class<R>) : this(clazz.kotlin)
}