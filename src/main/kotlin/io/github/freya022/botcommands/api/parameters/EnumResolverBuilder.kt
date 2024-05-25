package io.github.freya022.botcommands.api.parameters

import javax.annotation.CheckReturnValue

class EnumResolverBuilder<E : Enum<E>> internal constructor(
    private val e: Class<E>,
    private val guildValuesSupplier: EnumValuesSupplier<E>,
) {
    private var nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }

    /**
     * Sets the function transforming the enum value into the display name,
     * uses [Resolvers.toHumanName] by default.
     */
    @CheckReturnValue
    @JvmName("setNameFunction")
    fun nameFunction(function: EnumNameFunction<E>) = apply {
        nameFunction = function
    }

    @CheckReturnValue
    fun build(): ClassParameterResolver<*, E> {
        return EnumResolver(e, guildValuesSupplier, nameFunction)
    }
}