package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.serialization.exceptions.ComponentSerializationException
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.components.handler.options.ComponentHandlerOption
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.ConcurrentHashMap

internal sealed interface ComponentHandler

internal class PersistentHandler private constructor(val handlerName: String, val userData: List<String?>) : ComponentHandler {

    operator fun component1() = handlerName
    operator fun component2() = userData

    override fun toString(): String {
        return "PersistentHandler(handlerName='$handlerName')"
    }

    internal companion object {
        internal fun create(context: BContext, handlerName: String, userData: List<Any?>): PersistentHandler {
            return PersistentHandler(handlerName, processArgs(context, handlerName, userData))
        }

        internal fun fromData(handlerName: String, userData: List<String?>): PersistentHandler {
            return PersistentHandler(handlerName, userData)
        }

        private fun processArgs(context: BContext, handlerName: String, args: List<Any?>): List<String?> {
            val allOptionsOrdered = PersistentHandlerComponentDataOptionCache.getOrCreate(context, handlerName)

            return args.mapIndexed { index, arg ->
                if (arg == null) return@mapIndexed null

                val option = allOptionsOrdered[index]
                try {
                    @Suppress("UNCHECKED_CAST")
                    (option.resolver as ComponentParameterResolver<*, Any>).serialize(arg)
                } catch (e: ClassCastException) {
                    throw ComponentSerializationException("Argument #$index of value '$arg' is incompatible with parameter '${option.kParameter.findDeclarationName()}' of ${option.kParameter.function.shortSignature}", e)
                } catch (e: Exception) {
                    throw ComponentSerializationException("Exception while serializing '$arg'", e)
                }
            }
        }
    }
}

// It ain't much but hey
private object PersistentHandlerComponentDataOptionCache {
    private val cache = ConcurrentHashMap<String, List<ComponentHandlerOption>>()

    fun getOrCreate(context: BContext, handlerName: String): List<ComponentHandlerOption> {
        return cache.computeIfAbsent(handlerName) {
            val descriptor = context.getService<ComponentHandlerContainer>().getButtonDescriptor(handlerName)
                ?: throwArgument("No handler named '$handlerName' exists")
            descriptor.allOptionsOrdered.filterIsInstance<ComponentHandlerOption>().unmodifiableView()
        }
    }
}

internal class EphemeralHandler<T : GenericComponentInteractionCreateEvent> internal constructor(
    val handler: suspend (T) -> Unit
) : ComponentHandler {

    override fun toString(): String {
        return "EphemeralHandler(handler=${handler::class.simpleNestedName})"
    }
}