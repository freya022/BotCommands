package com.freya02.botcommands.internal.components.data;

import com.freya02.botcommands.api.components.SelectionConsumer;
import org.jetbrains.annotations.NotNull;

public class LambdaSelectionMenuData {
	private final SelectionConsumer consumer;

	public LambdaSelectionMenuData(@NotNull SelectionConsumer consumer) {
		this.consumer = consumer;
	}

	public SelectionConsumer getConsumer() {
		return consumer;
	}
}
