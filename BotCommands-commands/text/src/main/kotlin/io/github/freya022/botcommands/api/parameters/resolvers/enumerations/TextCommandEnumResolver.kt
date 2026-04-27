package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.TextCommandEnumResolverBuilderImpl

/**
 * Entry point to create enum resolver modules for text commands.
 */
object TextCommandEnumResolver {
    /**
     * Creates a new builder of an enum resolver module for text commands.
     *
     * It must be built then registered using [EnumResolverBuilder.with].
     */
    @JvmStatic
    fun <E : Enum<E>> builder(enumType: Class<E>): TextCommandEnumResolverBuilder<E> {
        return TextCommandEnumResolverBuilderImpl(enumType)
    }

    /**
     * Creates a new enum resolver module for text commands.
     *
     * It must be registered using [EnumResolverBuilder.with].
     */
    @JvmStatic
    fun <E : Enum<E>> of(enumType: Class<E>): EnumResolverModule<E> {
        return builder(enumType).build()
    }
}

/**
 * Register support for text command parameters.
 */
inline fun <reified E : Enum<E>> EnumResolverBuilder<E>.withTextCommands(crossinline block: TextCommandEnumResolverBuilder<E>.() -> Unit = {}): EnumResolverBuilder<E> {
    return with(
        TextCommandEnumResolver.builder(E::class.java)
            .apply(block)
            .build()
    )
}
