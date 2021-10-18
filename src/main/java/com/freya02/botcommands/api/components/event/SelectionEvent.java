package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;

public class SelectionEvent extends SelectionMenuEvent {
	private final BContext context;

	public SelectionEvent(BContext context, SelectionMenuEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}