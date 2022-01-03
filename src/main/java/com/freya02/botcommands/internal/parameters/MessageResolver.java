package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.MessageContextParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.Nullable;

public class MessageResolver extends ParameterResolver implements MessageContextParameterResolver {
	public MessageResolver() {
		super(Message.class);
	}

	@Nullable
	@Override
	public Object resolve(MessageContextInteractionEvent event) {
		return event.getTarget();
	}
}
