package com.freya02.botcommands.components.internal.data;

import com.freya02.botcommands.components.event.SelectionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LambdaSelectionMenuData {
	private final Consumer<SelectionEvent> consumer;

	public LambdaSelectionMenuData(@NotNull Consumer<SelectionEvent> consumer) {
		this.consumer = consumer;
	}

	public Consumer<SelectionEvent> getConsumer() {
		return consumer;
	}
}
