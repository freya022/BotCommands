package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.event.SelectionEvent;
import com.freya02.botcommands.api.components.SelectionConsumer;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LambdaSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<LambdaSelectionMenuBuilder>, LambdaComponentBuilder<LambdaSelectionMenuBuilder> {
	private final BContext context;
	private final SelectionConsumer consumer;

	private boolean oneUse;
	private LambdaComponentTimeoutInfo timeoutInfo = new LambdaComponentTimeoutInfo(0, TimeUnit.MILLISECONDS, () -> {});
	private final InteractionConstraints interactionConstraints = new InteractionConstraints();

	public LambdaSelectionMenuBuilder(BContext context, SelectionConsumer consumer) {
		super("fake");

		this.context = context;
		this.consumer = consumer;
	}

	public SelectionConsumer getConsumer() {
		return consumer;
	}

	@NotNull
	@Override
	public SelectionMenu build() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		setId(componentManager.putLambdaSelectionMenu(this));

		return super.build();
	}

	@Override
	public LambdaSelectionMenuBuilder oneUse() {
		this.oneUse = true;

		return this;
	}

	@Override
	public LambdaSelectionMenuBuilder timeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull Runnable timeoutCallback) {
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
