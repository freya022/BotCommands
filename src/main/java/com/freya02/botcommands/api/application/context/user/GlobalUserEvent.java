package com.freya02.botcommands.api.application.context.user;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.command.UserContextEvent;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import org.jetbrains.annotations.NotNull;

public class GlobalUserEvent extends UserContextEvent {
	private final BContext context;

	public GlobalUserEvent(BContext context, UserContextEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (UserContextInteraction) event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
