package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event

interface ICustomResolver {
    fun resolve(context: BContext, executableInteractionInfo: ExecutableInteractionInfo, event: Event): Any? =
        TODO("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, executableInteractionInfo: ExecutableInteractionInfo, event: Event) =
        resolve(context, executableInteractionInfo, event)
}