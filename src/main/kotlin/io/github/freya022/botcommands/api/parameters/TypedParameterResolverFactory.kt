package io.github.freya022.botcommands.api.parameters

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

abstract class TypedParameterResolverFactory<T : ParameterResolver<T, R>, R : Any>(
    resolverType: KClass<out T>,
    protected val type: KType
) : ParameterResolverFactory<T, R>(resolverType) {
    constructor(resolverType: Class<T>, type: Class<*>) : this(resolverType.kotlin, type.kotlin.starProjectedType)

    override fun isResolvable(type: KType): Boolean {
        return this.type == type
    }
}