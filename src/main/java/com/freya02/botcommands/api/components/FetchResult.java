package com.freya02.botcommands.api.components;

import org.jetbrains.annotations.Nullable;

public abstract class FetchResult implements AutoCloseable {
	private final FetchedComponent fetchedComponent;

	protected FetchResult(@Nullable FetchedComponent fetchedComponent) {
		this.fetchedComponent = fetchedComponent;
	}

	@Nullable
	public FetchedComponent getFetchedComponent() {
		return fetchedComponent;
	}
}
