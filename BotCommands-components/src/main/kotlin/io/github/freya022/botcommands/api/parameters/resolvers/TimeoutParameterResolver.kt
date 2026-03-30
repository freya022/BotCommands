package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableTimeoutData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.emoji.Emoji
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Resolver for parameters annotated with [@TimeoutData][TimeoutData].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * ### Types supported by default
 * - [String]
 * - [Boolean]
 * - [Int]
 * - [Long]
 * - [Double]
 * - [Emoji]
 * - [UserSnowflake]
 *
 * ### Use case - Supporting serializable objects
 * If you need to pass **serializable** objects to your components,
 * you can instead use [@SerializableTimeoutData][SerializableTimeoutData]
 * and let it generate a resolver for you.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface TimeoutParameterResolver<T, R : Any> : IParameterResolver<T>, ResolverMarker
        where T : ParameterResolver<T, R>,
              T : TimeoutParameterResolver<T, R> {

    /**
     * Returns a resolved object for this argument.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored.
     *
     * @param option The option currently being resolved
     * @param data   A serialized representation of an argument passed in [IPersistentTimeoutableComponent.timeout]
     */
    fun resolve(option: TimeoutOption, data: SerializedComponentData): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this argument.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored.
     *
     * @param option The option currently being resolved
     * @param data   A serialized representation of an argument passed in [IPersistentTimeoutableComponent.timeout]
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): R? =
        resolve(option, data)

    /**
     * Serializes an instance of the resolvable object.
     *
     * The given instance can be serialized in any way you want,
     * remember you must be able to deserialize it in [resolve]/[resolveSuspend].
     */
    fun serialize(obj: R): SerializedComponentData
}
