package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.event.SelectionEvent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LambdaSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<LambdaSelectionMenuBuilder>, LambdaComponentBuilder<LambdaSelectionMenuBuilder> {
	private final BContext context;
	private final Consumer<SelectionEvent> consumer;

	private boolean oneUse;
	private long ownerId;
	private LambdaComponentTimeoutInfo timeoutInfo = new LambdaComponentTimeoutInfo(0, TimeUnit.MILLISECONDS, () -> {});

	public LambdaSelectionMenuBuilder(BContext context, Consumer<SelectionEvent> consumer) {
		super("fake");

		this.context = context;
		this.consumer = consumer;
	}

	public Consumer<SelectionEvent> getConsumer() {
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
	public LambdaSelectionMenuBuilder ownerId(long ownerId) {
		this.ownerId = ownerId;

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
	public long getOwnerId() {
		return ownerId;
	}

	@Override
	public LambdaComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}
}
