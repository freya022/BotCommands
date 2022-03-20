package com.freya02.botcommands.api.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.prefixed.HelpConsumer;
import com.freya02.botcommands.api.prefixed.TextCommandFilter;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
	 * Disables the help command for prefixed commands and replaces the implementation when incorrect syntax is detected<br>
	 * <b>You can provide an empty implementation if you wish to just disable all the help stuff completely</b>
	 *
	 * @param helpConsumer Consumer used to show help when a command is detected but their syntax is invalid, can do nothing
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder disableHelpCommand(@NotNull HelpConsumer helpConsumer) {
		this.context.overrideHelp(helpConsumer);

		return this;
	}

	/**
	 * Sets the help builder consumer, it allows you to add stuff in the help embeds when they are created.
	 *
	 * @param builderConsumer The help builder consumer, modifies the EmbedBuilder
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder setHelpBuilderConsumer(Consumer<EmbedBuilder> builderConsumer) {
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
