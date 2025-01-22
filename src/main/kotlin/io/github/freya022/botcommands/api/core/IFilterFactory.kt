package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.annotations.FilterFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.ReflectionUtils
import kotlin.reflect.KFunction

/**
 * Factory interface to create filters on each function the annotation [A] is applied on.
 *
 * These factories are accepted by [@FilterFactory][FilterFactory].
 *
 * **Usage**: Register your instance as a service with [@BService][BService].
 *
 * @see FilterFactory @FilterFactory
 * @see create
 */
@InterfacedService(acceptMultiple = true)
interface IFilterFactory<A : Annotation> {

    /**
     * Creates a [Filter] out of the given function and annotation.
     *
     * Tip: You can convert from a `KFunction` to a `Method`/`Constructor`/`Executable` using [ReflectionUtils].
     *
     * @param function   The function which has this annotation applied to
     * @param annotation The [@FilterFactory][FilterFactory] instance
     */
    fun create(function: KFunction<*>, annotation: A): Filter
}