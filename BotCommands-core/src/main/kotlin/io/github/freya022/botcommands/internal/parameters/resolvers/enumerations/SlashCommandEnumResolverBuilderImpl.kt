package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumNameFunction
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverModule
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.SlashCommandEnumResolver.ValuesSupplier
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.SlashCommandEnumResolverBuilder

internal class SlashCommandEnumResolverBuilderImpl<E : Enum<E>>(
    private val enumType: Class<E>,
) : SlashCommandEnumResolverBuilder<E> {

    private var valuesSupplier: ValuesSupplier<E>? = null
    private var nameFunction: EnumNameFunction<E>? = null

    override fun overrideValues(supplier: ValuesSupplier<E>): SlashCommandEnumResolverBuilder<E> {
        this.valuesSupplier = supplier
        return this
    }

    override fun overrideNameFunction(nameFunction: EnumNameFunction<E>): SlashCommandEnumResolverBuilder<E> {
        this.nameFunction = nameFunction
        return this
    }

    override fun build(): EnumResolverModule<E> {
        return Module(enumType, valuesSupplier, nameFunction)
    }

    private class Module<E : Enum<E>>(
        private val enumType: Class<E>,
        private var guildValuesSupplier: ValuesSupplier<E>?,
        private var nameFunctionOverride: EnumNameFunction<E>?,
    ) : EnumResolverModuleMixin<E> {

        override fun createResolver(base: EnumResolverBuilderImpl.AsImmutable<E>): ClassParameterResolver<*, E> {
            return SlashCommandEnumResolverImpl(
                enumType,
                guildValuesSupplier ?: ValuesSupplier { base.values },
                nameFunctionOverride ?: base.nameFunction,
            )
        }
    }
}
