package com.freya02.botcommands.internal.components.data;

import com.freya02.botcommands.api.components.SelectionConsumer;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class LambdaSelectionMenuData<E extends GenericSelectMenuInteractionEvent<?, ?>> {
	private final SelectionConsumer<E> consumer;

	public LambdaSelectionMenuData(@NotNull SelectionConsumer<E> consumer) {
		this.consumer = consumer;
	}

	public SelectionConsumer<E> getConsumer() {
		return consumer;
	}
}
