package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.SlashCommandEnumResolver.ValuesSupplier
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.annotations.EnumDSL

/**
 * Builder for the enum resolver module for slash commands.
 *
 * @see SlashCommandEnumResolver.builder
 */
@EnumDSL
interface SlashCommandEnumResolverBuilder<E : Enum<E>> {
    /**
     * Overrides the available values, can be supplied on a per-guild basis.
     *
     * By default, the values are inherited from [EnumResolverBuilder].
     *
     * @param supplier The per-guild values supplier
     */
    fun overrideValues(supplier: ValuesSupplier<E>): SlashCommandEnumResolverBuilder<E>

    /**
     * Overrides the available values.
     *
     * By default, the values are inherited from [EnumResolverBuilder].
     *
     * @param values The values to instead use
     *
     * @throws IllegalArgumentException If [values] is empty.
     */
    fun overrideValues(values: Collection<E>): SlashCommandEnumResolverBuilder<E> =
        overrideValues(supplier = { values })

    /**
     * Overrides the available values.
     *
     * By default, the values are inherited from [EnumResolverBuilder].
     *
     * @param values The values to instead use
     */
    fun overrideValues(value: E, vararg values: E): SlashCommandEnumResolverBuilder<E> =
        overrideValues(listOf(value, *values))

    /**
     * Overrides the name mapping function.
     *
     * The returned names will be what the user will see.
     *
     * By default, this is inherited from [EnumResolverBuilder].
     *
     * @param nameFunction The function returning the display name of the given enum value
     */
    fun overrideNameFunction(nameFunction: EnumNameFunction<E>): SlashCommandEnumResolverBuilder<E>

    /**
     * Builds the module, it must be [registered][EnumResolverBuilder.with].
     */
    fun build(): EnumResolverModule<E>
}
