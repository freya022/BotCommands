package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaType

/**
 * Specialization of [ParameterResolverFactory] for a specific [KType].
 * Java users can supply a [KotlinTypeToken] instead.
 *
 * Your implementation needs to be annotated with [@ResolverFactory][ResolverFactory].
 *
 * You can also create a service factory using [resolverFactory].
 *
 * @see ParameterResolverFactory
 * @see resolverFactory
 *
 * @param type Type of the objects returned by the parameter resolver
 */
abstract class TypedParameterResolverFactory(
    val type: KType
) : ParameterResolverFactory() {
    override val supportedTypesStr: List<String> = listOf(type.shortQualifiedName)

    constructor(type: KClass<*>) : this(type.starProjectedType)
    constructor(type: Class<*>) : this(type.kotlin.starProjectedType)
    constructor(typeToken: KotlinTypeToken<*>) : this(typeToken.type)

    init {
        require(!type.isMarkedNullable) {
            "Typed parameter resolver factories cannot have a nullable type"
        }
    }

    override fun isResolvable(request: ResolverRequest): Boolean {
        val requestedType = request.parameter.type
        return this.type == requestedType
                // Resolver of type T can resolve parameters of type T?
                || this.type == requestedType.withNullability(false)
                // Improves Java interoperability
                // Prevents issues when the resolver is for a k.c.List and Java parameter is a j.u.List
                // KType#javaType may have a few unsupported cases (it uses KType#stdlibJavaType),
                // while I believe it won't affect anyone,
                // it's still used as a last resort, just in case
                || this.type.javaType == requestedType.javaType
    }
}
