package com.freya02.botcommands.slash.impl;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.slash.SlashEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.interactions.CommandInteractionImpl;

public class SlashEventImpl extends SlashEvent {
	private final BContext context;

	public SlashEventImpl(BContext context, SlashCommandEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (CommandInteractionImpl) event.getInteraction());
		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}
