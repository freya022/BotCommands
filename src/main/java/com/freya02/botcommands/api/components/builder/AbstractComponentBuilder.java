package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.components.InteractionConstraints;

@SuppressWarnings("unchecked")
public abstract class AbstractComponentBuilder<T extends AbstractComponentBuilder<T>> implements ComponentBuilder<T> {
	private final InteractionConstraints interactionConstraints = new InteractionConstraints();
	private boolean oneUse;

	public T oneUse() {
		this.oneUse = true;

		return (T) this;
	}

	public boolean isOneUse() {
		return oneUse;
	}

	@Override
	public InteractionConstraints getInteractionConstraints() {
		return interactionConstraints;
	}
}