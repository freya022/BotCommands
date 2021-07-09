package com.freya02.botcommands.components.builder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class ComponentBuilderImpl<T extends ComponentBuilderImpl<T>> implements ComponentBuilder<T> {
	private boolean oneUse;
	private long ownerId;

	private long expirationTimestamp;

	@Override
	public void oneUse() {
		this.oneUse = true;
	}

	@Override
	public T ownerId(long ownerId) {
		this.ownerId = ownerId;

		return (T) this;
	}

	@Override
	public T timeout(long timeout, TimeUnit timeoutUnit) {
		this.expirationTimestamp = timeoutUnit.toSeconds(timeout);

		return (T) this;
	}

	@Override
	public T expireOn(LocalDateTime time) {
		this.expirationTimestamp = time.toEpochSecond(ZoneOffset.UTC);

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
	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}
}
