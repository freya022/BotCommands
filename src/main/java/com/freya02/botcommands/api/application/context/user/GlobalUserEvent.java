package com.freya02.botcommands.api.application.context.user;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.UserCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public class GlobalUserEvent extends UserContextCommandEvent {
	private final BContext context;

	public GlobalUserEvent(BContext context, UserContextCommandEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (UserCommandInteractionImpl) event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
