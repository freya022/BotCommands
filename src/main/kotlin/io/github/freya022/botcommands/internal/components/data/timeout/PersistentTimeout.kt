package io.github.freya022.botcommands.internal.components.data.timeout

import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.exceptions.ComponentSerializationException
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.timeout.ComponentTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.GroupTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.options.TimeoutHandlerOption
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import java.util.concurrent.ConcurrentHashMap

internal class PersistentTimeout private constructor(
    val handlerName: String,
    val userData: List<SerializedComponentData?>
) : ComponentTimeout {
    internal companion object {
        internal fun create(context: BContext, componentType: ComponentType, handlerName: String, userData: List<Any?>): PersistentTimeout {
            return PersistentTimeout(
                handlerName,
                processArgs(context, componentType, handlerName, userData)
            )
        }

        internal fun fromData(handlerName: String, userData: List<ByteArray?>): PersistentTimeout {
            return PersistentTimeout(
                handlerName,
                userData.map { it?.let(SerializedComponentData::fromBytes) }
            )
        }

        private fun processArgs(context: BContext, componentType: ComponentType, handlerName: String, args: List<Any?>): List<SerializedComponentData?> {
            val allOptionsOrdered = PersistentTimeoutComponentDataOptionCache.getOrCreate(context, componentType, handlerName)

            return args.mapIndexed { index, arg ->
                if (arg == null) return@mapIndexed null

                val option = allOptionsOrdered[index]
                try {
                    @Suppress("UNCHECKED_CAST")
                    (option.resolver as TimeoutParameterResolver<*, Any>).serialize(arg)
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
private object PersistentTimeoutComponentDataOptionCache {
    private val cache = ConcurrentHashMap<CacheKey, List<TimeoutHandlerOption>>()

    private data class CacheKey(private val componentType: ComponentType, private val handlerName: String)

    fun getOrCreate(context: BContext, componentType: ComponentType, handlerName: String): List<TimeoutHandlerOption> {
        return cache.computeIfAbsent(CacheKey(componentType, handlerName)) {
            val container = when (componentType) {
                ComponentType.GROUP -> context.getService<GroupTimeoutHandlers>()
                else -> context.getService<ComponentTimeoutHandlers>()
            }

            val descriptor = container[handlerName] ?: throwArgument("No timeout handler named '$handlerName' exists")
            descriptor.allOptionsOrdered.filterIsInstance<TimeoutHandlerOption>().unmodifiableView()
        }
    }
}