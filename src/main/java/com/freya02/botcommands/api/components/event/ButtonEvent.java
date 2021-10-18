package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonEvent extends ButtonClickEvent {
	private final BContext context;

	public ButtonEvent(BContext context, ButtonClickEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}
