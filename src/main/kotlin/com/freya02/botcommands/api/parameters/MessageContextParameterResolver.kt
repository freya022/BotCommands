package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

interface MessageContextParameterResolver<T : ParameterResolver<T, R>, R> {
    /**
     * Returns a resolved object from this message context interaction
     *
     * @param context The [BContext] of this bot
     * @param info    The message command info of the command being executed
     * @param event   The event of this message context interaction
     * @return The resolved option mapping
     */
    fun resolve(context: BContext, info: MessageCommandInfo, event: MessageContextInteractionEvent): R? =
        throw UnsupportedOperationException("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, info: MessageCommandInfo, event: MessageContextInteractionEvent) =
        resolve(context, info, event)
}