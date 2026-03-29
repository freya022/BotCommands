package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.core.utils.toImmutableEnumSet
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumNameFunction
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverModule
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.TextCommandEnumResolverBuilder

internal class TextCommandEnumResolverBuilderImpl<E : Enum<E>>(
    private val enumType: Class<E>
) : TextCommandEnumResolverBuilder<E> {

    private var values: Set<E>? = null
    private var nameFunction: EnumNameFunction<E>? = null
    private var ignoreCase: Boolean = true

    override fun overrideValues(values: Collection<E>): TextCommandEnumResolverBuilder<E> {
        require(values.isNotEmpty()) { "Enum values must not be empty" }
        this.values = values.toImmutableEnumSet(enumType)
        return this
    }

    override fun overrideNameFunction(nameFunction: EnumNameFunction<E>): TextCommandEnumResolverBuilder<E> {
        this.nameFunction = nameFunction
        return this
    }

    override fun ignoreCase(ignoreCase: Boolean): TextCommandEnumResolverBuilder<E> {
        this.ignoreCase = ignoreCase
        return this
    }

    override fun build(): EnumResolverModule<E> {
        return Module(enumType, values, nameFunction, ignoreCase)
    }

    private class Module<E : Enum<E>>(
        private val enumType: Class<E>,
        private var valuesOverride: Set<E>?,
        private var nameFunctionOverride: EnumNameFunction<E>?,
        private var ignoreCase: Boolean
    ) : EnumResolverModuleMixin<E> {

        override fun createResolver(base: EnumResolverBuilderImpl.AsImmutable<E>): ClassParameterResolver<*, E> {
            return TextCommandEnumResolverImpl(
                enumType,
                valuesOverride ?: base.values,
                nameFunctionOverride ?: base.nameFunction,
                ignoreCase,
            )
        }
    }
}
