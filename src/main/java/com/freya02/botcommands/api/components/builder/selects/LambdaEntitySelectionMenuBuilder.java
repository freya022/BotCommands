package com.freya02.botcommands.api.components.builder.selects;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.EntitySelectionConsumer;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.builder.LambdaComponentTimeoutInfo;
import com.freya02.botcommands.api.components.event.EntitySelectionEvent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class LambdaEntitySelectionMenuBuilder
		extends EntitySelectMenu.Builder
		implements LambdaSelectionMenuBuilder<LambdaEntitySelectionMenuBuilder, EntitySelectionEvent> {
	private final BContext context;
	private final EntitySelectionConsumer consumer;

	private boolean oneUse;
	private LambdaComponentTimeoutInfo timeoutInfo = new LambdaComponentTimeoutInfo(0, TimeUnit.MILLISECONDS, () -> {});
	private final InteractionConstraints interactionConstraints = new InteractionConstraints();

	public LambdaEntitySelectionMenuBuilder(Collection<SelectTarget> types, BContext context, EntitySelectionConsumer consumer) {
		super("fake");
		setEntityTypes(types);

		this.context = context;
		this.consumer = consumer;
	}

	@NotNull
	@Override
	public EntitySelectionConsumer getConsumer() {
		return consumer;
	}

	@NotNull
	@Override
	public EntitySelectMenu build() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		setId(componentManager.putLambdaSelectMenu(this));

		return super.build();
	}

	@Override
	public LambdaEntitySelectionMenuBuilder oneUse() {
		this.oneUse = true;

		return this;
	}

	@Override
	public LambdaEntitySelectionMenuBuilder timeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull Runnable timeoutCallback) {
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
