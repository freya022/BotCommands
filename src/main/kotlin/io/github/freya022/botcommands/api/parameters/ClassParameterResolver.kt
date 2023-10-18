package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Base class for parameter resolvers using static types.
 *
 * This is best suited if you want a simple resolver for a specific type, such as [TimeUnit] for example.
 *
 * @see TypedParameterResolver
 */
@InterfacedService(acceptMultiple = true)
abstract class ClassParameterResolver<T : ClassParameterResolver<T, R>, R : Any>(
    val jvmErasure: KClass<R>
) : ParameterResolver<T, R>() {
    constructor(clazz: Class<R>) : this(clazz.kotlin)

    override fun toString(): String {
        return "ClassParameterResolver(jvmErasure=$jvmErasure)"
    }
}