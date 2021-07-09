package com.freya02.botcommands.components.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.components.event.ButtonEvent;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.function.Consumer;

public class LambdaButtonBuilder extends ComponentBuilderImpl<LambdaButtonBuilder> {
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
		final ComponentManager idManager = Utils.getComponentManager(context);

		return idManager.putLambdaButton(this);
	}
}
