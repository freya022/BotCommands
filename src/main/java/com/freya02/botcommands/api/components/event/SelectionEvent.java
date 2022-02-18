package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;

public class SelectionEvent extends SelectMenuInteractionEvent {
	private final BContext context;

	public SelectionEvent(BContext context, SelectMenuInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}