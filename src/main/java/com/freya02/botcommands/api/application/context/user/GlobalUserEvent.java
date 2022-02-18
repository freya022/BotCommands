package com.freya02.botcommands.api.application.context.user;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class GlobalUserEvent extends UserContextInteractionEvent {
	private final BContext context;

	public GlobalUserEvent(BContext context, UserContextInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
