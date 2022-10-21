package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.StringSelectionConsumer;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LambdaStringSelectionMenuBuilder
		extends StringSelectMenu.Builder
		implements LambdaSelectionMenuBuilder<LambdaStringSelectionMenuBuilder, StringSelectionEvent> {
	private final BContext context;
	private final StringSelectionConsumer consumer;

	private boolean oneUse;
	private LambdaComponentTimeoutInfo timeoutInfo = new LambdaComponentTimeoutInfo(0, TimeUnit.MILLISECONDS, () -> {});
	private final InteractionConstraints interactionConstraints = new InteractionConstraints();

	public LambdaStringSelectionMenuBuilder(BContext context, StringSelectionConsumer consumer) {
		super("fake");

		this.context = context;
		this.consumer = consumer;
	}

	@NotNull
	@Override
	public StringSelectionConsumer getConsumer() {
		return consumer;
	}

	@NotNull
	@Override
	public StringSelectMenu build() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		setId(componentManager.putLambdaSelectMenu(this));

		return super.build();
	}

	@Override
	public LambdaStringSelectionMenuBuilder oneUse() {
		this.oneUse = true;

		return this;
	}

	@Override
	public LambdaStringSelectionMenuBuilder timeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull Runnable timeoutCallback) {
		this.timeoutInfo = new LambdaComponentTimeoutInfo(timeout, timeoutUnit, timeoutCallback);

		return this;
	}

	@Override
	public boolean isOneUse() {
		return oneUse;
	}

	@Override
	public InteractionConstraints getInteractionConstraints() {
		return interactionConstraints;
	}

	@Override
	public LambdaComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}
}
