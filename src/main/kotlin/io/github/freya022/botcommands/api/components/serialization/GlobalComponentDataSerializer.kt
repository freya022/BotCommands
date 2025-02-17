package io.github.freya022.botcommands.api.components.serialization

import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableTimeoutData
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Serializes and deserializes data from parameters annotated with [@SerializableComponentData][SerializableComponentData]
 * and [@SerializableTimeoutData][SerializableTimeoutData].
 *
 * ### Default implementation
 * By default, a Jackson-based serializer with the Kotlin module is used.
 *
 * ### Overriding the default instance
 * You can override the default instance by creating a service implementing this interface,
 * in which you can use any serialization library you want.
 *
 * **Tip:** You will generally need to get the type of the to-be-deserialized parameter,
 * which you can get from the [ParameterWrapper].
 *
 * Here's an example with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization):
 *
 * ```kt
 * @BService
 * class KotlinxComponentDataSerializer : GlobalComponentDataSerializer {
 *
 *     // Default instance, you can customize it later
 *     private val json = Json
 *
 *     override fun deserialize(parameter: ParameterWrapper, data: SerializedComponentData): Any {
 *         return json.decodeFromString(serializer(parameter.type), data.asString())!!
 *     }
 *
 *     override fun serialize(parameter: ParameterWrapper, obj: Any): SerializedComponentData {
 *         val json = json.encodeToString(serializer(parameter.type), obj)
 *         return SerializedComponentData.fromString(json)
 *     }
 * }
 * ```
 */
@InterfacedService(acceptMultiple = false)
interface GlobalComponentDataSerializer {

    /**
     * Serializes the given object into a [SerializedComponentData].
     *
     * @param parameter The parameter which this value is serialized for
     * @param obj       The data to be serialized
     */
    fun serialize(parameter: ParameterWrapper, obj: Any): SerializedComponentData

    /**
     * Deserializes the [data] into an object compatible with the [parameter].
     *
     * @param parameter The parameter which this value is deserialized for
     * @param data      The data to be deserialized into a compatible object
     */
    fun deserialize(parameter: ParameterWrapper, data: SerializedComponentData): Any
}