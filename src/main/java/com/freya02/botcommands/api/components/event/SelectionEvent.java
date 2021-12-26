package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.component.SelectionMenuEvent;

public class SelectionEvent extends SelectionMenuEvent { //TODO remove at some point, if people want a BContext they can use a custom parameter
	private final BContext context;

	public SelectionEvent(BContext context, SelectionMenuEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}