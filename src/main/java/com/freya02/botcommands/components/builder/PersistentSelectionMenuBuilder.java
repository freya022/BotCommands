package com.freya02.botcommands.components.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.ComponentManager;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class PersistentSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<PersistentSelectionMenuBuilder>, PersistentComponentBuilder {
	private final BContext context;
	private final String handlerName;
	private final String[] args;

	private boolean oneUse;
	private long ownerId;
	private long expirationTimestamp;

	public PersistentSelectionMenuBuilder(BContext context, String handlerName, String[] args) {
		super("fake");

		this.context = context;
		this.handlerName = handlerName;
		this.args = args;
	}

	@Override
	public String getHandlerName() {
		return handlerName;
	}

	@Override
	public String[] getArgs() {
		return args;
	}

	@Nonnull
	@Override
	public SelectionMenu build() {
		final ComponentManager idManager = Utils.getComponentManager(context);

		setId(idManager.putPersistentSelectionMenu(this));

		return super.build();
	}

	@Override
	public PersistentSelectionMenuBuilder oneUse() {
		this.oneUse = true;

		return this;
	}

	@Override
	public PersistentSelectionMenuBuilder ownerId(long ownerId) {
		this.ownerId = ownerId;

		return this;
	}

	@Override
	public PersistentSelectionMenuBuilder timeout(long timeout, TimeUnit timeoutUnit) {
		this.expirationTimestamp = timeoutUnit.toMillis(timeout);

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
	public long getTimeout() {
		return expirationTimestamp;
	}
}
