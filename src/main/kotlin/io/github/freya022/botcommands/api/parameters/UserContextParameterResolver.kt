package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

interface UserContextParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                         T : UserContextParameterResolver<T, R> {
    /**
     * Returns a resolved object from this user context interaction
     *
     * @param info    The user command info of the command being executed
     * @param event   The event of this user context interaction
     * @return The resolved option mapping
     */
    fun resolve(info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(info: UserCommandInfo, event: UserContextInteractionEvent) =
        resolve(info, event)
}