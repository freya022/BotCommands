package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

public interface LambdaComponentBuilder<T extends LambdaComponentBuilder<T>> {
	T timeout(long timeout, TimeUnit timeoutUnit, Runnable timeoutCallback);

	LambdaComponentTimeoutInfo getTimeout();
}
