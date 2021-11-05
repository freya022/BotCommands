package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

public record PersistentComponentTimeoutInfo(long timeout, TimeUnit timeoutUnit) {
	public long toMillis() {
		return timeoutUnit.toMillis(timeout);
	}
}
