package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.function.Consumer;

public class LambdaButtonBuilder extends AbstractComponentBuilder<LambdaButtonBuilder> {
	private final BContext context;
	private final Consumer<ButtonEvent> consumer;
	private final ButtonStyle buttonStyle;

	public LambdaButtonBuilder(BContext context, Consumer<ButtonEvent> consumer, ButtonStyle buttonStyle) {
		this.context = context;
		this.consumer = consumer;
		this.buttonStyle = buttonStyle;
	}

	public Consumer<ButtonEvent> getConsumer() {
		return consumer;
	}

	public Button build(String label) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, null).withLabel(label);
	}

	public Button build(Emoji emoji) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, null).withEmoji(emoji);
	}

	public String buildId() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		return componentManager.putLambdaButton(this);
	}
}
