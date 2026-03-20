package io.github.freya022.botcommands.internal.components.serialization.resolver

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableTimeoutData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.hasAnnotation
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.utils.annotationRef
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@ResolverFactory
internal class SerializableComponentDataResolverFactory(
    private val globalSerializer: GlobalComponentDataSerializer,
) : ParameterResolverFactory<SerializableComponentDataResolverFactory.Resolver>(Resolver::class) {

    override val supportedTypesStr: List<String> =
        listOf("<any ${annotationRef<SerializableComponentData>()} or ${annotationRef<SerializableTimeoutData>()} parameter>")

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        listOf(ComponentParameterResolver::class.java, TimeoutParameterResolver::class.java)

    override fun isResolvable(request: ResolverRequest): Boolean {
        return request.parameter.hasAnnotation<SerializableComponentData>() || request.parameter.hasAnnotation<SerializableTimeoutData>()
    }

    override fun get(request: ResolverRequest): Resolver {
        return Resolver(request.parameter, globalSerializer)
    }

    internal class Resolver(
        private val parameter: ParameterWrapper,
        private val globalSerializer: GlobalComponentDataSerializer,
    ) : ClassParameterResolver<Resolver, Any>(Any::class),
        ComponentParameterResolver<Resolver, Any>,
        TimeoutParameterResolver<Resolver, Any> {

        override suspend fun resolveSuspend(
            option: ComponentOption,
            event: GenericComponentInteractionCreateEvent,
            data: SerializedComponentData
        ): Any = globalSerializer.deserialize(parameter, data)

        override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Any =
            globalSerializer.deserialize(parameter, data)

        override fun serialize(obj: Any): SerializedComponentData = globalSerializer.serialize(parameter, obj)
    }
}
