package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event

interface ICustomResolver<T : ParameterResolver<T, R>, R> {
    fun resolve(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event): R? =
        throw UnsupportedOperationException("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event) =
        resolve(context, executableInteractionInfo, event)
}