package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;
import org.jetbrains.annotations.Nullable;

public class MessageResolver extends ParameterResolver implements MessageContextParameterResolver {
	public MessageResolver() {
		super(Message.class);
	}

	@Nullable
	@Override
	public Object resolve(MessageContextCommandEvent event) {
		return event.getTargetMessage();
	}
}
