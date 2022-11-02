package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.components.ComponentConsumer;

import java.util.concurrent.TimeUnit;

public interface LambdaComponentBuilder<T extends LambdaComponentBuilder<T, C>, C extends ComponentConsumer<?>> extends ComponentBuilder<T> {
	/**
	 * Makes this component expire after the specified timeout
	 * <br>Once the component expires it should be removed from the component manager
	 * <br>Additionally, components which are in the same group as this component are also deleted
	 *
	 * @return This component builder for chaining purposes
	 */
	T timeout(long timeout, TimeUnit timeoutUnit, Runnable timeoutCallback);

	LambdaComponentTimeoutInfo getTimeout();

	C getConsumer();
}
