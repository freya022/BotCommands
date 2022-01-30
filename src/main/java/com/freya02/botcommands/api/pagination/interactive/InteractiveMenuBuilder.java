package com.freya02.botcommands.api.pagination.interactive;

import org.jetbrains.annotations.NotNull;

/**
 * Builds an {@link InteractiveMenu}
 */
public final class InteractiveMenuBuilder extends BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu> {
	@Override
	@NotNull
	public InteractiveMenu build() {
		return new InteractiveMenu(items, constraints, timeout);
	}
}
