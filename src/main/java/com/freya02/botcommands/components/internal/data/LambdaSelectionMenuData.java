package com.freya02.botcommands.components.internal.data;

import com.freya02.botcommands.components.event.SelectionEvent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class LambdaSelectionMenuData {
	private final Consumer<SelectionEvent> consumer;

	public LambdaSelectionMenuData(@Nonnull Consumer<SelectionEvent> consumer) {
		this.consumer = consumer;
	}

	public Consumer<SelectionEvent> getConsumer() {
		return consumer;
	}
}
