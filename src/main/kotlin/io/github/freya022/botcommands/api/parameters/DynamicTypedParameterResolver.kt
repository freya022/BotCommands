package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlin.reflect.KType

/**
 * @see ParameterResolverFactory
 */
@InterfacedService(acceptMultiple = true)
abstract class DynamicTypedParameterResolver<T : DynamicTypedParameterResolver<T, R>, R : Any> : ParameterResolver<T, R>() {
    abstract fun isResolvable(type: KType): Boolean
}