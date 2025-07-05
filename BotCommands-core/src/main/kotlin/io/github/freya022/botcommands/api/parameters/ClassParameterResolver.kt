package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Base class for parameter resolvers using static types.
 *
 * This is best suited if you want a simple resolver for a specific type, such as [TimeUnit] for example.
 *
 * Your implementation needs to be annotated with [@Resolver][Resolver],
 * unless this is an output of a [ParameterResolverFactory].
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 *
 * @see TypedParameterResolver
 */
@InterfacedService(acceptMultiple = true)
abstract class ClassParameterResolver<T : ClassParameterResolver<T, R>, R : Any>(
    val jvmErasure: KClass<out R>
) : ParameterResolver<T, R>() {
    constructor(clazz: Class<out R>) : this(clazz.kotlin)

    override fun toString(): String {
        return "ClassParameterResolver(jvmErasure=$jvmErasure)"
    }
}