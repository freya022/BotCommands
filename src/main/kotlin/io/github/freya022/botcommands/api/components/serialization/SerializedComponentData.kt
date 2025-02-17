package io.github.freya022.botcommands.api.components.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData.Companion.fromBytes
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData.Companion.fromString
import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver

/**
 * Contains the serialized data of a component argument.
 *
 * @see fromString
 * @see fromBytes
 *
 * @see ComponentParameterResolver.serialize
 */
class SerializedComponentData private constructor(
    private val bytes: ByteArray,
) {

    /**
     * Returns the underlying byte array.
     */
    fun asBytes(): ByteArray = bytes.clone()

    /**
     * Decodes the data as a UTF-8 string.
     */
    fun asString() = bytes.decodeToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedComponentData

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "SerializedComponentData(bytes=${bytes.contentToString()})"
    }

    companion object {

        /**
         * Creates a [SerializedComponentData] from the given bytes.
         */
        @JvmStatic
        fun fromBytes(bytes: ByteArray): SerializedComponentData {
            return SerializedComponentData(bytes.clone())
        }

        /**
         * Creates a [SerializedComponentData] from the given string.
         */
        @JvmStatic
        fun fromString(string: String): SerializedComponentData {
            return SerializedComponentData(string.encodeToByteArray())
        }
    }
}

/**
 * Serializes the given [value] as a [SerializedComponentData] encoded as a UTF-8 string.
 */
fun ObjectMapper.writeValueAsComponentData(value: Any): SerializedComponentData =
    fromBytes(writeValueAsBytes(value))

/**
 * Deserializes the given UTF-8 encoded JSON object [data] as a [T] instance.
 */
inline fun <reified T : Any> ObjectMapper.readValue(data: SerializedComponentData): T =
    readValue(data.asBytes())

/**
 * Deserializes the given UTF-8 encoded JSON object [data] as a [T] instance.
 */
fun <T : Any> ObjectMapper.readValue(data: SerializedComponentData, typeToken: KotlinTypeToken<T>): T =
    readValue(data.asBytes(), constructType(typeToken.javaType))