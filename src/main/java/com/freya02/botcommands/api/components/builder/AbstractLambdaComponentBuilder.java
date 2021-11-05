package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class AbstractLambdaComponentBuilder<T extends AbstractLambdaComponentBuilder<T>> extends AbstractComponentBuilder<T> implements LambdaComponentBuilder<T> {
	private LambdaComponentTimeoutInfo timeoutInfo;

	/**
	 * Makes this component expire after the specified timeout<br>
	 * Once the component expires it should be removed from the component manager
	 *
	 * @return This component builder for chaining purposes
	 */
	@Override
	public T timeout(long timeout, TimeUnit timeoutUnit, Runnable timeoutCallback) {
		this.timeoutInfo = new LambdaComponentTimeoutInfo(timeout, timeoutUnit, timeoutCallback);

		return (T) this;
	}

	public LambdaComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}
}
