package dev.freya02.botcommands.typesafe.messages.internal.resolvers

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.exceptions.throwInternal
import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.superErasureAt
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@ResolverFactory
internal class MessageSourceResolverFactory(
    private val messageSourceFactories: List<IMessageSourceFactory<*>>,
) : ParameterResolverFactory<MessageSourceResolver>(MessageSourceResolver::class) {

    override val supportedTypesStr: List<String> = listOf("<out MessageSource>")

    override fun isResolvable(request: ResolverRequest): Boolean {
        // Is it requesting a message source?
        val parameterErasure = request.parameter.erasure
        if (!parameterErasure.isSubclassOf<IMessageSource>()) return false

        require(request.isFromCompatibleHandler()) {
            "Only text commands and interaction handlers can be injected with IMessageSource instances"
        }

        // It is requesting a message source, throw if we don't find a corresponding factory
        if (findMessageSourceFactory(parameterErasure) != null) {
            return true
        } else if (messageSourceFactories.isEmpty()) {
            throw IllegalArgumentException("No IMessageSourceFactory producing ${parameterErasure.shortQualifiedName} instances was found, as no factory exists")
        } else {
            val availableFactories = messageSourceFactories.joinAsList { factory ->
                val sourceType = factory::class.superErasureAt<IMessageSourceFactory<*>>(0)
                "${factory.javaClass.shortQualifiedName} -> ${sourceType.shortQualifiedName}"
            }
            throw IllegalArgumentException("No IMessageSourceFactory producing ${parameterErasure.shortQualifiedName} instances was found, available factories:\n$availableFactories")
        }
    }

    override fun get(request: ResolverRequest): MessageSourceResolver {
        val factory = findMessageSourceFactory(expectedSourceType = request.parameter.erasure)
            ?: throwInternal("Unable to find back message source after it was greenlit by isResolvable")

        return MessageSourceResolver(factory)
    }

    private fun ResolverRequest.isFromCompatibleHandler(): Boolean {
        val declaringFunction = parameter.parameter.function
        val firstParameter = declaringFunction.valueParameters.firstOrNull() ?: return false

        val erasure = firstParameter.type.jvmErasure
        return erasure.isSubclassOf<Interaction>() || erasure.isSubclassOf<MessageReceivedEvent>()
    }

    private fun findMessageSourceFactory(expectedSourceType: KClass<*>): IMessageSourceFactory<*>? {
        for (factory in messageSourceFactories) {
            val sourceType = factory::class.superErasureAt<IMessageSourceFactory<*>>(0)
            if (sourceType.jvmErasure == expectedSourceType) {
                return factory
            }
        }

        return null
    }
}

internal class MessageSourceResolver(
    private val factory: IMessageSourceFactory<*>,
) : ClassParameterResolver<MessageSourceResolver, IMessageSource>(IMessageSource::class),
    ICustomResolver<MessageSourceResolver, IMessageSource> {

    override suspend fun resolveSuspend(
        option: Option,
        event: Event,
    ): IMessageSource {
        return when (event) {
            is Interaction -> factory.create(event)
            is MessageReceivedEvent -> factory.create(event)
            else -> throwInternal("Unhandled event type: $event")
        }
    }
}
