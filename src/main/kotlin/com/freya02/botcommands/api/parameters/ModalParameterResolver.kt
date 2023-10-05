package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.internal.modals.ModalHandlerInfo
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.ModalMapping

interface ModalParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                             T : ModalParameterResolver<T, R> {
    /**
     * Returns a resolved object for this [ModalMapping]
     *
     * @param context      The [BContext] of this bot
     * @param info         The modal handler info of the command being executed
     * @param event        The event of this modal interaction
     * @param modalMapping The [ModalMapping] to be resolved
     * @return The resolved option mapping
     */
    fun resolve(context: BContext, info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, info: ModalHandlerInfo, event: ModalInteractionEvent, modalMapping: ModalMapping) =
        resolve(context, info, event, modalMapping)
}