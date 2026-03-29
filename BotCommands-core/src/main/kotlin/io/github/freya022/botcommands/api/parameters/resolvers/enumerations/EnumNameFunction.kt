package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

/**
 * Transforms an enum entry into a human-readable name.
 */
fun interface EnumNameFunction<E : Enum<E>> {
    fun apply(value: E): String
}
