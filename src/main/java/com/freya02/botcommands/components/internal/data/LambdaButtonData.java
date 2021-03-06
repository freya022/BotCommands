package com.freya02.botcommands.components.internal.data;

import com.freya02.botcommands.components.event.ButtonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LambdaButtonData {
	private final Consumer<ButtonEvent> consumer;

	public LambdaButtonData(@NotNull Consumer<ButtonEvent> consumer) {
		this.consumer = consumer;
	}

	public Consumer<ButtonEvent> getConsumer() {
		return consumer;
	}
}
