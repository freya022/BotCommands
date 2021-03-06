package com.freya02.botcommands.components.builder;

import java.util.concurrent.TimeUnit;

public interface ComponentBuilder<T extends ComponentBuilder<T>> {
	T oneUse();

	T ownerId(long ownerId);

	T timeout(long timeout, TimeUnit timeoutUnit);

	boolean isOneUse();

	long getOwnerId();

	long getTimeout();
}
