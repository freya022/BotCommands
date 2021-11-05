package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

public record LambdaComponentTimeoutInfo(long timeout, TimeUnit timeoutUnit, Runnable timeoutCallback) {
	public long toMillis() {
		return timeoutUnit.toMillis(timeout);
	}
}
