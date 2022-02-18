package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;

public class GlobalSlashEventImpl extends GlobalSlashEvent {
	private final BContext context;

	public GlobalSlashEventImpl(BContext context, SlashCommandInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (SlashCommandInteractionImpl) event.getInteraction());
		
		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}
