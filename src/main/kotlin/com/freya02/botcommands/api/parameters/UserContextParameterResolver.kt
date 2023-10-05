package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

interface UserContextParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                                   T : UserContextParameterResolver<T, R> {
    /**
     * Returns a resolved object from this user context interaction
     *
     * @param context The [BContext] of this bot
     * @param info    The user command info of the command being executed
     * @param event   The event of this user context interaction
     * @return The resolved option mapping
     */
    fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent) = resolve(context, info, event)
}