package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.commands.application.context.message.options.MessageContextCommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

@Resolver
internal class MessageResolver : ClassParameterResolver<MessageResolver, Message>(Message::class),
                                 MessageContextParameterResolver<MessageResolver, Message> {

    override suspend fun resolveSuspend(
        option: MessageContextCommandOption,
        event: MessageContextInteractionEvent,
    ): Message = event.target
}
