package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.context.message.options.MessageContextCommandOption;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Resolver
public class MessageResolver
        extends ClassParameterResolver<MessageResolver, Message>
        implements MessageContextParameterResolver<MessageResolver, Message> {

    public MessageResolver() {
        super(Message.class);
    }

    @Nullable
    @Override
    public Message resolve(@NotNull MessageContextCommandOption option, @NotNull MessageContextInteractionEvent event) {
        return event.getTarget();
    }
}
