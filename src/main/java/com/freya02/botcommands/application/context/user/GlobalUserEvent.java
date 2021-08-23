package com.freya02.botcommands.application.context.user;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.UserCommandInteractionImpl;

import javax.annotation.Nonnull;

public class GlobalUserEvent extends UserContextCommandEvent {
	private final BContext context;

	public GlobalUserEvent(BContext context, UserContextCommandEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (UserCommandInteractionImpl) event.getInteraction());

		this.context = context;
	}

	@Nonnull
	public BContext getContext() {
		return context;
	}
}
