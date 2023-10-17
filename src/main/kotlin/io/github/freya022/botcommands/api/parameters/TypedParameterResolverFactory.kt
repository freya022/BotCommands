package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

abstract class TypedParameterResolverFactory<T : ParameterResolver<T, *>>(
    resolverType: KClass<out T>,
    protected val type: KType
) : ParameterResolverFactory<T>(resolverType) {
    override val supportedTypesStr: List<String> = listOf(type.simpleNestedName)

    constructor(resolverType: Class<T>, type: Class<*>) : this(resolverType.kotlin, type.kotlin.starProjectedType)

    override fun isResolvable(type: KType): Boolean {
        return this.type == type
    }
}