package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class MapResolver<R : Map<*, *>>(
    mapType: KType
) : TypedParameterResolver<MapResolver<R>, R>(mapType),
    ICustomResolver<MapResolver<R>, R>

object StringDoubleMapResolver : MapResolver<Map<String, Double>>(typeOf<Map<String, Double>>()) {
    override suspend fun resolveSuspend(info: IExecutableInteractionInfo, event: Event): Map<String, Double> =
        mapOf("lol" to 3.14159)
}

@ResolverFactory
object MapResolverFactory : ParameterResolverFactory<MapResolver<*>>(MapResolver::class) {
    private val resolvers = listOf(StringDoubleMapResolver)

    override val supportedTypesStr: List<String> = resolvers.map { it.type.simpleNestedName }

    override fun isResolvable(parameter: ParameterWrapper): Boolean {
        return resolvers.any { it.type == parameter.type }
    }

    override fun get(parameter: ParameterWrapper): MapResolver<*> {
        return resolvers.first { it.type == parameter.type }
    }
}