package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@ModalHandler][ModalHandler].
 */
interface ModalParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                   T : ModalParameterResolver<T, R> {
    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param info         The data of the modal handler being executed
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    fun resolve(info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param info         The data of the modal handler being executed
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping) =
        resolve(info, event, modalMapping)
}