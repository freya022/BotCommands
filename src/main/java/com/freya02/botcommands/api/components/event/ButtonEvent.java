package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class ButtonEvent extends ButtonInteractionEvent {
	private final BContext context;

	public ButtonEvent(BContextImpl context, ButtonInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}
}
