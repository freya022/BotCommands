package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageResolver extends ParameterResolver implements MessageContextParameterResolver {
	public MessageResolver() {
		super(ParameterType.ofClass(Message.class));
	}

	@Nullable
	@Override
	public Object resolve(@NotNull BContext context, @NotNull MessageCommandInfo info, @NotNull MessageContextInteractionEvent event) {
		return event.getTarget();
	}
}
