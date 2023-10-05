package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.ModalMapping

interface ModalParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                             T : ModalParameterResolver<T, R> {
    /**
     * Returns a resolved object for this [ModalMapping]
     *
     * @param info         The modal handler info of the command being executed
     * @param event        The event of this modal interaction
     * @param modalMapping The [ModalMapping] to be resolved
     * @return The resolved option mapping
     */
    fun resolve(info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping) =
        resolve(info, event, modalMapping)
}