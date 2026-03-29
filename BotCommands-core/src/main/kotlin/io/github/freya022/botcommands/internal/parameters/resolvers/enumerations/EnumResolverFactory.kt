package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.utils.throwInternal

internal class EnumResolverFactory<E : Enum<E>> internal constructor(
    private val enumType: Class<E>,
    private val resolvers: List<ClassParameterResolver<*, E>>,
    private val declarationSiteSignature: String,
) : ParameterResolverFactory() {

    override val supportedTypesStr: List<String> = listOf(enumType.shortQualifiedName)
    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        resolvers.flatMap { inferSupportedResolversFrom(it.javaClass) }

    override fun isResolvable(request: ResolverRequest): Boolean {
        return request.parameter.javaErasure == enumType && resolvers.any { request.resolverType.isInstance(it) }
    }

    override fun get(request: ResolverRequest): IParameterResolver<*> {
        for (resolver in resolvers) {
            if (request.resolverType.isInstance(resolver)) {
                return resolver
            }
        }

        throwInternal("No matching ${request.resolverType.shortQualifiedName} for ${request.parameter.parameter}")
    }

    override fun toLogString(): String {
        return "$declarationSiteSignature ; priority $priority (${supportedTypesStr.single()})"
    }
}
