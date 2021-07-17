package com.freya02.botcommands.components.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.ComponentManager;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

public class PersistentButtonBuilder extends ComponentBuilderImpl<PersistentButtonBuilder> implements PersistentComponentBuilder {
	private final BContext context;
	private final String handlerName;
	private final String[] args;
	private final ButtonStyle buttonStyle;

	public PersistentButtonBuilder(BContext context, String handlerName, String[] args, ButtonStyle buttonStyle) {
		this.context = context;
		this.handlerName = handlerName;
		this.args = args;
		this.buttonStyle = buttonStyle;
	}

	@Override
	public String getHandlerName() {
		return handlerName;
	}

	@Override
	public String[] getArgs() {
		return args;
	}

	public Button build(String label) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, null).withLabel(label);
	}

	public Button build(Emoji emoji) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, null).withEmoji(emoji);
	}

	public String buildId() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		return componentManager.putPersistentButton(this);
	}
}
