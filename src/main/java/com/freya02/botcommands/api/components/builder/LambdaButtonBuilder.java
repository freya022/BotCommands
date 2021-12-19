package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ButtonConsumer;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.utils.ButtonContent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

public class LambdaButtonBuilder extends AbstractLambdaComponentBuilder<LambdaButtonBuilder> {
	private final BContext context;
	private final ButtonConsumer consumer;
	private final ButtonStyle buttonStyle;

	public LambdaButtonBuilder(BContext context, ButtonConsumer consumer, ButtonStyle buttonStyle) {
		this.context = context;
		this.consumer = consumer;
		this.buttonStyle = buttonStyle;
	}

	public ButtonConsumer getConsumer() {
		return consumer;
	}

	public Button build(String label) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, null).withLabel(label);
	}

	public Button build(Emoji emoji) {
		return new ButtonImpl(buildId(), "", buttonStyle, false, emoji);
	}

	public Button build(ButtonContent content) {
		//Build either a button with a label, an emoji, or both

		if (content.text() != null) {
			if (content.emoji() != null) { //both
				return new ButtonImpl(buildId(), "", buttonStyle, false, content.emoji()).withLabel(content.text());
			} else { //label
				return new ButtonImpl(buildId(), "", buttonStyle, false, null).withLabel(content.text());
			}
		} else { //emoji
			return new ButtonImpl(buildId(), "", buttonStyle, false, content.emoji());
		}
	}

	public String buildId() {
		final ComponentManager componentManager = Utils.getComponentManager(context);

		return componentManager.putLambdaButton(this);
	}
}
