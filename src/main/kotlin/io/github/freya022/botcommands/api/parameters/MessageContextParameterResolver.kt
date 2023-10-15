package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

interface MessageContextParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                            T : MessageContextParameterResolver<T, R> {
    /**
     * Returns a resolved object from this message context interaction
     *
     * @param info    The message command info of the command being executed
     * @param event   The event of this message context interaction
     * @return The resolved option mapping
     */
    fun resolve(info: MessageCommandInfo, event: MessageContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(info: MessageCommandInfo, event: MessageContextInteractionEvent) =
        resolve(info, event)
}