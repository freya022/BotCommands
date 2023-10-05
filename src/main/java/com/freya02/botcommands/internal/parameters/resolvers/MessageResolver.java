package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.core.BContext;
import com.freya02.botcommands.api.core.service.annotations.Resolver;
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo;
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
