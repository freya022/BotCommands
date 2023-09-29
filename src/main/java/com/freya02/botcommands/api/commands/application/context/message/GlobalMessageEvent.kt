package com.freya02.botcommands.api.commands.application.context.message;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class GlobalMessageEvent extends MessageContextInteractionEvent {
	private final BContext context;

	public GlobalMessageEvent(BContextImpl context, MessageContextInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
