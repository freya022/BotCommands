package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ICustomResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class MapResolver<R : Map<*, *>>(
    val mapType: KType
) : TypedParameterResolver<MapResolver<R>, R>(mapType),
    ICustomResolver<MapResolver<R>, R>

object StringDoubleMapResolver : MapResolver<Map<String, Double>>(typeOf<Map<String, Double>>()) {
    override suspend fun resolveSuspend(
        executableInteractionInfo: IExecutableInteractionInfo,
        event: Event
    ): Map<String, Double> = mapOf("lol" to 3.14159)
}

@ResolverFactory
object MapResolverFactory : ParameterResolverFactory<MapResolver<*>>(MapResolver::class) {
    private val resolvers = listOf(StringDoubleMapResolver)

    override fun isResolvable(type: KType): Boolean {
        return resolvers.any { it.mapType == type }
    }

    override fun get(parameter: ParameterWrapper): MapResolver<*> {
        return resolvers.first { it.mapType == parameter.type }
    }
}