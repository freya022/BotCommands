package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.exceptions.ComponentSerializationException
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.handler.options.ComponentHandlerOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.ConcurrentHashMap

internal sealed interface ComponentHandler

internal class PersistentHandler private constructor(val handlerName: String, val userData: List<SerializedComponentData?>) : ComponentHandler {

    operator fun component1() = handlerName
    operator fun component2() = userData

    override fun toString(): String {
        return "PersistentHandler(handlerName='$handlerName')"
    }

    internal companion object {
        internal fun create(context: BContext, componentType: ComponentType, handlerName: String, userData: List<Any?>): PersistentHandler {
            return PersistentHandler(handlerName, processArgs(context, componentType, handlerName, userData))
        }

        internal fun fromData(handlerName: String, userData: List<ByteArray?>): PersistentHandler {
            return PersistentHandler(handlerName, userData.map { it?.let(SerializedComponentData::fromBytes) })
        }

        private fun processArgs(context: BContext, componentType: ComponentType, handlerName: String, args: List<Any?>): List<SerializedComponentData?> {
            val allOptionsOrdered = PersistentHandlerComponentDataOptionCache.getOrCreate(context, componentType, handlerName)

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

    fun getOrCreate(context: BContext, componentType: ComponentType, handlerName: String): List<ComponentHandlerOption> {
        return cache.computeIfAbsent(handlerName) {
            val container = context.getService<ComponentHandlerContainer>()
            val descriptor = when (componentType) {
                ComponentType.GROUP -> throwInternal("Tried to retrieve an action component descriptor but the type is a group")
                ComponentType.BUTTON -> container.getButtonDescriptor(handlerName)
                    ?: throwArgument("No ${annotationRef<JDAButtonListener>()} named '$handlerName' exists")
                ComponentType.SELECT_MENU -> container.getSelectMenuDescriptor(handlerName)
                    ?: throwArgument("No ${annotationRef<JDASelectMenuListener>()} named '$handlerName' exists")
            }
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