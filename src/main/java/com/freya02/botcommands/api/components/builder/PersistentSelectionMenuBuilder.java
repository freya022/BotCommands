package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PersistentSelectionMenuBuilder extends SelectionMenu.Builder implements ComponentBuilder<PersistentSelectionMenuBuilder>, PersistentComponentBuilder<PersistentSelectionMenuBuilder> {
	private final BContext context;
	private final String handlerName;
	private final String[] args;

	private boolean oneUse;
	private PersistentComponentTimeoutInfo timeoutInfo = new PersistentComponentTimeoutInfo(0, TimeUnit.MILLISECONDS);
	private final InteractionConstraints interactionConstraints = new InteractionConstraints();

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

	@NotNull
	@Override
	public SelectionMenu build() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		setId(componentManager.putPersistentSelectionMenu(this));

		return super.build();
	}

	@Override
	public PersistentSelectionMenuBuilder oneUse() {
		this.oneUse = true;

		return this;
	}

	/**
	 * Makes this component expire after the specified timeout<br>
	 * Once the component expires it should be removed from the component manager
	 *
	 * @return This component builder for chaining purposes
	 */
	@Override
	public PersistentSelectionMenuBuilder timeout(long timeout, @NotNull TimeUnit timeoutUnit) {
		this.timeoutInfo = new PersistentComponentTimeoutInfo(timeout, timeoutUnit);

		return this;
	}

	@Override
	public boolean isOneUse() {
		return oneUse;
	}

	@Override
	public PersistentComponentTimeoutInfo getTimeout() {
		return timeoutInfo;
	}

	@Override
	public InteractionConstraints getInteractionConstraints() {
		return interactionConstraints;
	}
}
