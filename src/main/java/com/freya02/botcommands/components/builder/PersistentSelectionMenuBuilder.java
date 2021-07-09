package com.freya02.botcommands.components.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.ComponentManager;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PersistentSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<PersistentSelectionMenuBuilder>, PersistentComponentBuilder {
	private final BContext context;
	private final String handlerName;
	private final List<String> args;

	private boolean oneUse;
	private long ownerId;
	private long expirationTimestamp;

	public PersistentSelectionMenuBuilder(BContext context, String handlerName, List<String> args) {
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
	public List<String> getArgs() {
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
	public void oneUse() {
		this.oneUse = true;
	}

	@Override
	public PersistentSelectionMenuBuilder ownerId(long ownerId) {
		this.ownerId = ownerId;

		return this;
	}

	@Override
	public PersistentSelectionMenuBuilder timeout(long timeout, TimeUnit timeoutUnit) {
		this.expirationTimestamp = timeoutUnit.toSeconds(timeout);

		return this;
	}

	@Override
	public PersistentSelectionMenuBuilder expireOn(LocalDateTime time) {
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
