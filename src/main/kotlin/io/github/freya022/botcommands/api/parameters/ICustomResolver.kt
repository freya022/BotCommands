package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event

interface ICustomResolver<T, R> where T : ParameterResolver<T, R>,
                                      T : ICustomResolver<T, R> {
    fun resolve(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event) =
        resolve(context, executableInteractionInfo, event)
}