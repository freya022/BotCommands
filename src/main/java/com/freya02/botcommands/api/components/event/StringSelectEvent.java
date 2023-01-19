package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class StringSelectEvent extends StringSelectInteractionEvent {
	private final BContext context;

	public StringSelectEvent(BContextImpl context, StringSelectInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}