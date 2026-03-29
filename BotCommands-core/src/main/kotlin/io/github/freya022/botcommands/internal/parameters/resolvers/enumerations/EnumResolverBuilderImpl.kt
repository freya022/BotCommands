package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.core.utils.toImmutableEnumSet
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumNameFunction
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverModule
import io.github.freya022.botcommands.api.parameters.toHumanName
import java.util.*

class EnumResolverBuilderImpl<E : Enum<E>> internal constructor(
    private val enumType: Class<E>,
    private val declarationSiteSignature: String,
) : EnumResolverBuilder<E> {

    private val moduleProviders: MutableList<EnumResolverModule<E>> = mutableListOf()

    // All values by default, restrict with this, can be overridden per-module
    var values: Set<E> = EnumSet.allOf(enumType)
        private set

    var nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }
        private set

    override fun setValues(values: Collection<E>): EnumResolverBuilder<E> {
        require(values.isNotEmpty()) { "Enum values must not be empty" }
        this.values = values.toImmutableEnumSet(enumType)
        return this
    }

    override fun setNameFunction(nameFunction: EnumNameFunction<E>): EnumResolverBuilder<E> {
        this.nameFunction = nameFunction
        return this
    }

    override fun with(moduleProvider: EnumResolverModule<E>): EnumResolverBuilder<E> {
        moduleProviders.add(moduleProvider)
        return this
    }

    internal fun build(): ParameterResolverFactory {
        val asImmutable = AsImmutable(values, nameFunction)
        val moduleResolvers = moduleProviders.map { (it as EnumResolverModuleMixin<E>).createResolver(asImmutable) }

        return EnumResolverFactory(enumType, moduleResolvers, declarationSiteSignature)
    }

    class AsImmutable<E : Enum<E>>(
        val values: Set<E>,
        val nameFunction: EnumNameFunction<E>,
    )
}
