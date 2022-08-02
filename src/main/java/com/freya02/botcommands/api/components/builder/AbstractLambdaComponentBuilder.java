package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.components.ComponentConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class AbstractLambdaComponentBuilder<T extends AbstractLambdaComponentBuilder<T, C>, C extends ComponentConsumer<?>> extends AbstractComponentBuilder<T> implements LambdaComponentBuilder<T, C> {
	private LambdaComponentTimeoutInfo timeoutInfo = new LambdaComponentTimeoutInfo(0, TimeUnit.MILLISECONDS, () -> {});

	@Override
	public T timeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull Runnable timeoutCallback) {
		this.timeoutInfo = new LambdaComponentTimeoutInfo(timeout, timeoutUnit, timeoutCallback);

		return (T) this;
	}

	public LambdaComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}
}
