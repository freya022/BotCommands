package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.annotations.EnumDSL

/**
 * Builder for an enum resolver module for text commands.
 *
 * @see TextCommandEnumResolver.builder
 */
@EnumDSL
interface TextCommandEnumResolverBuilder<E : Enum<E>> {
    /**
     * Overrides the available values.
     *
     * By default, the values are inherited from [EnumResolverBuilder].
     *
     * @param values The values to instead use
     *
     * @throws IllegalArgumentException If [values] is empty.
     */
    fun overrideValues(values: Collection<E>): TextCommandEnumResolverBuilder<E>

    /**
     * Overrides the available values.
     *
     * By default, the values are inherited from [EnumResolverBuilder].
     *
     * @param values The values to instead use
     */
    fun overrideValues(value: E, vararg values: E): TextCommandEnumResolverBuilder<E> =
        overrideValues(listOf(value, *values))

    /**
     * Overrides the name mapping function.
     *
     * The returned names will be what the user needs to type. Case-sensitiveness is controlled by [ignoreCase].
     *
     * By default, this is inherited from [EnumResolverBuilder].
     *
     * @param nameFunction The function returning the display name of the given enum value
     */
    fun overrideNameFunction(nameFunction: EnumNameFunction<E>): TextCommandEnumResolverBuilder<E>

    /**
     * Configures whether the user input can be matched to the expected value.
     *
     * By default, this is `true`.
     *
     * @param ignoreCase Whether arguments will be matched while ignoring the casing, `true` by default
     */
    fun ignoreCase(ignoreCase: Boolean): TextCommandEnumResolverBuilder<E>

    /**
     * Builds the module, it must be [registered][EnumResolverBuilder.with].
     */
    fun build(): EnumResolverModule<E>
}
