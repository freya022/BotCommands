package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.Resolvers
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.annotations.EnumDSL

/**
 * Builder of an enum resolver.
 *
 * @see ResolverManager.registerEnum
 * @see Resolvers.ofEnum
 */
@EnumDSL
interface EnumResolverBuilder<E : Enum<E>> {

    /**
     * Sets the values available for a user to input. This will be inherited by added modules.
     *
     * By default, all enum values are available.
     */
    fun setValues(values: Collection<E>): EnumResolverBuilder<E>

    /**
     * Sets the values available for a user to input. This will be inherited by added modules.
     *
     * By default, all enum values are available.
     */
    fun setValues(value: E, vararg values: E): EnumResolverBuilder<E> =
        setValues(listOf(value, *values))

    /**
     * Overrides the name mapping function. This is used to get the user-facing name of a value.
     *
     * By default, this uses [Resolvers.toHumanName][io.github.freya022.botcommands.api.parameters.Resolvers.toHumanName].
     *
     * @param nameFunction The function returning the display name of the given enum value
     */
    fun setNameFunction(nameFunction: EnumNameFunction<E>): EnumResolverBuilder<E>

    /**
     * Registers the module provider,
     * this effectively enables resolving arguments of a specific handler type,
     * such as slash commands.
     */
    fun with(moduleProvider: EnumResolverModule<E>): EnumResolverBuilder<E>
}
