package com.freya02.botcommands.application.context.message;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.MessageCommandInteractionImpl;

import javax.annotation.Nonnull;

public class GlobalMessageEvent extends MessageContextCommandEvent {
	private final BContext context;

	public GlobalMessageEvent(BContext context, MessageContextCommandEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (MessageCommandInteractionImpl) event.getInteraction());

		this.context = context;
	}

	@Nonnull
	public BContext getContext() {
		return context;
	}
}
