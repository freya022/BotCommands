package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@ModalHandler][ModalHandler].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface ModalParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : ModalParameterResolver<T, R> {
    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    fun resolve(event: ModalEvent, modalMapping: ModalMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(event: ModalEvent, modalMapping: ModalMapping) =
        resolve(event, modalMapping)
}