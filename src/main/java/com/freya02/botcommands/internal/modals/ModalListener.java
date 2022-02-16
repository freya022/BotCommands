package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class ModalListener implements EventListener {
	private final BContextImpl context;

	public ModalListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onEvent(@NotNull GenericEvent e) {
		if (e instanceof ModalInteractionEvent event) {
			final ModalData modalData = context.getModalMaps().getModalData(event.getModalId());

			context.getModalHandler(event.getModalId());
		}
	}
}
