package com.freya02.botcommands.api.application.context.message;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.command.MessageContextEvent;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import org.jetbrains.annotations.NotNull;

public class GlobalMessageEvent extends MessageContextEvent {
	private final BContext context;

	public GlobalMessageEvent(BContext context, MessageContextEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (MessageContextInteraction) event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
