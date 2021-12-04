package com.freya02.botcommands.api.components;

import org.jetbrains.annotations.NotNull;

public interface FetchedComponent extends AutoCloseable {
	@NotNull
	ComponentType getType();
}
