package com.freya02.botcommands.internal.components.data;

import com.freya02.botcommands.api.components.ButtonConsumer;
import org.jetbrains.annotations.NotNull;

public class LambdaButtonData {
	private final ButtonConsumer consumer;

	public LambdaButtonData(@NotNull ButtonConsumer consumer) {
		this.consumer = consumer;
	}

	public ButtonConsumer getConsumer() {
		return consumer;
	}
}
