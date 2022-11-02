package com.freya02.botcommands.api.components.builder;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public record PersistentComponentTimeoutInfo(long timeout, @NotNull TimeUnit timeoutUnit) implements ComponentTimeoutInfo {
	@Override
	public long toMillis() {
		return timeoutUnit.toMillis(timeout);
	}
}
