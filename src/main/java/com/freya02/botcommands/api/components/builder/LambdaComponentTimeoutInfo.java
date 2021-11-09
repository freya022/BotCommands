package com.freya02.botcommands.api.components.builder;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public record LambdaComponentTimeoutInfo(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull Runnable timeoutCallback) {
	public long toMillis() {
		return timeoutUnit.toMillis(timeout);
	}
}
