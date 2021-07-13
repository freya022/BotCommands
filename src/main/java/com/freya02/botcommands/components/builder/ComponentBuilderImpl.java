package com.freya02.botcommands.components.builder;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class ComponentBuilderImpl<T extends ComponentBuilderImpl<T>> implements ComponentBuilder<T> {
	private boolean oneUse;
	private long ownerId;

	private long timeout;

	@Override
	public T oneUse() {
		this.oneUse = true;

		return (T) this;
	}

	@Override
	public T ownerId(long ownerId) {
		this.ownerId = ownerId;

		return (T) this;
	}

	@Override
	public T timeout(long timeout, TimeUnit timeoutUnit) {
		this.timeout = timeoutUnit.toMillis(timeout);

		return (T) this;
	}

	@Override
	public boolean isOneUse() {
		return oneUse;
	}

	@Override
	public long getOwnerId() {
		return ownerId;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}
}