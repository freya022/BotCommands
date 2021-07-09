package com.freya02.botcommands.components.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.components.event.SelectionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LambdaSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<LambdaSelectionMenuBuilder> {
	private final BContext context;
	private final Consumer<SelectionEvent> consumer;

	private boolean oneUse;
	private long ownerId;
	private long expirationTimestamp;

	public LambdaSelectionMenuBuilder(BContext context, Consumer<SelectionEvent> consumer) {
		super("fake");

		this.context = context;
		this.consumer = consumer;
	}

	public Consumer<SelectionEvent> getConsumer() {
		return consumer;
	}

	@Nonnull
	@Override
	public SelectionMenu build() {
		final ComponentManager idManager = Utils.getComponentManager(context);

		setId(idManager.putLambdaSelectionMenu(this));

		return super.build();
	}

	@Override
	public void oneUse() {
		this.oneUse = true;
	}

	@Override
	public LambdaSelectionMenuBuilder ownerId(long ownerId) {
		this.ownerId = ownerId;

		return this;
	}

	@Override
	public LambdaSelectionMenuBuilder timeout(long timeout, TimeUnit timeoutUnit) {
		this.expirationTimestamp = timeoutUnit.toSeconds(timeout);

		return this;
	}

	@Override
	public LambdaSelectionMenuBuilder expireOn(LocalDateTime time) {
		this.expirationTimestamp = time.toEpochSecond(ZoneOffset.UTC);

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
	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}
}
