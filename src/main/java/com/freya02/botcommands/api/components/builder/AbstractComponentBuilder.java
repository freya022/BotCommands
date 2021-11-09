package com.freya02.botcommands.api.components.builder;

@SuppressWarnings("unchecked")
public abstract class AbstractComponentBuilder<T extends AbstractComponentBuilder<T>> implements ComponentBuilder<T> {
	private boolean oneUse;
	private long ownerId;

	public T oneUse() {
		this.oneUse = true;

		return (T) this;
	}

	public T ownerId(long ownerId) {
		this.ownerId = ownerId;

		return (T) this;
	}

	public boolean isOneUse() {
		return oneUse;
	}

	public long getOwnerId() {
		return ownerId;
	}
}