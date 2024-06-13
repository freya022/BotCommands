package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.commands.Executable
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.core.service.annotations.RequiresDefaultInjection
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class MapResolver<R : Map<*, *>>(
    mapType: KType
) : TypedParameterResolver<MapResolver<R>, R>(mapType),
    ICustomResolver<MapResolver<R>, R>

object StringDoubleMapResolver : MapResolver<Map<String, Double>>(typeOf<Map<String, Double>>()) {
    override suspend fun resolveSuspend(executable: Executable, event: Event): Map<String, Double> =
        mapOf("lol" to 3.14159)
}

@ResolverFactory
@RequiresDefaultInjection
class MapResolverFactory : ParameterResolverFactory<MapResolver<*>>(MapResolver::class) {
    private val resolvers = listOf(StringDoubleMapResolver)

    override val supportedTypesStr: List<String> = resolvers.map { it.type.simpleNestedName }

    override fun isResolvable(request: ResolverRequest): Boolean {
        return resolvers.any { it.type == request.parameter.type }
    }

    override fun get(request: ResolverRequest): MapResolver<*> {
        return resolvers.first { it.type == request.parameter.type }
    }
}