package com.freya02.botcommands.application.slash.impl;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.application.slash.GlobalSlashEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.SlashCommandInteractionImpl;

public class GlobalSlashEventImpl extends GlobalSlashEvent {
	private final BContext context;

	public GlobalSlashEventImpl(BContext context, SlashCommandEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (SlashCommandInteractionImpl) event.getInteraction());
		
		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}
