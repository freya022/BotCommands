package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class AbstractPersistentComponentBuilder<T extends AbstractPersistentComponentBuilder<T>> extends AbstractComponentBuilder<T> implements PersistentComponentBuilder<T> {
	private PersistentComponentTimeoutInfo timeoutInfo = new PersistentComponentTimeoutInfo(0, TimeUnit.MILLISECONDS);

	/**
	 * Makes this component expire after the specified timeout<br>
	 * Once the component expires it should be removed from the component manager
	 *
	 * @return This component builder for chaining purposes
	 */
	public T timeout(long timeout, TimeUnit timeoutUnit) {
		this.timeoutInfo = new PersistentComponentTimeoutInfo(timeout, timeoutUnit);

		return (T) this;
	}

	public PersistentComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}
}
