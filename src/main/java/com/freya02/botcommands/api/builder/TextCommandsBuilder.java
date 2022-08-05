package com.freya02.botcommands.api.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.prefixed.HelpBuilderConsumer;
import com.freya02.botcommands.api.prefixed.TextCommandFilter;
import com.freya02.botcommands.internal.BContextImpl;

public final class TextCommandsBuilder {
	private final BContextImpl context;

	public TextCommandsBuilder(BContextImpl context) {
		this.context = context;
	}

	/**
	 * Adds a prefix to choose from the list of prefixes
	 *
	 * @param prefix The prefix to add
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder addPrefix(String prefix) {
		context.addPrefix(prefix);

		return this;
	}

	/**
	 * Disables the built-in help command for prefixed commands.
	 * <br>This still lets you implement a custom help command.
	 *
	 * @param isHelpDisabled <code>true</code> to disable the built-in help command
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder disableHelpCommand(boolean isHelpDisabled) {
		this.context.disableHelp(isHelpDisabled);

		return this;
	}

	/**
	 * Sets the help builder consumer, it allows you to add stuff in the help embeds when they are created.
	 * <br>This is called everytime a help embed is generated, when using the default help command
	 *
	 * @param builderConsumer The help builder consumer, modifies the EmbedBuilder
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder setHelpBuilderConsumer(HelpBuilderConsumer builderConsumer) {
		context.setHelpBuilderConsumer(builderConsumer);

		return this;
	}

	/**
	 * Adds a filter for received messages (could prevent regular commands from running), <b>See {@link BContext#addTextFilter(TextCommandFilter)} for more info</b>
	 *
	 * @param filter The filter to add, should return <code>false</code> if the message has to be ignored
	 * @return This builder for chaining convenience
	 * @see BContext#addTextFilter(TextCommandFilter)
	 */
	public TextCommandsBuilder addTextFilter(TextCommandFilter filter) {
		context.addTextFilter(filter);

		return this;
	}
}
