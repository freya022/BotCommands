package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.MessageContextParameterResolver;
import io.github.freya022.botcommands.api.parameters.ParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Resolver
public class MessageResolver
		extends ParameterResolver<MessageResolver, Message>
		implements MessageContextParameterResolver<MessageResolver, Message> {

	public MessageResolver() {
		super(Message.class);
	}

	@Nullable
	@Override
	public Message resolve(@NotNull BContext context, @NotNull MessageCommandInfo info, @NotNull MessageContextInteractionEvent event) {
		return event.getTarget();
	}
}
