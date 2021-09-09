package com.freya02.botcommands.builder;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.MessageInfo;
import com.freya02.botcommands.prefixed.annotation.AddExecutableHelp;
import com.freya02.botcommands.prefixed.annotation.AddSubcommandHelp;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

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
	 * Enables {@linkplain AddSubcommandHelp} on all registered commands
	 *
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder addSubcommandHelpByDefault() {
		context.setAddSubcommandHelpByDefault(true);

		return this;
	}

	/**
	 * Enables {@linkplain AddExecutableHelp} on all registered commands
	 *
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder addExecutableHelpByDefault() {
		context.setAddExecutableHelpByDefault(true);

		return this;
	}

	/**
	 * Disables the help command for prefixed commands and replaces the implementation when incorrect syntax is detected<br>
	 * <b>You can provide an empty implementation if you wish to just disable all the help stuff completely</b>
	 *
	 * @param helpConsumer Consumer used to show help when a command is detected but their syntax is invalid, can do nothing
	 * @return This builder for chaining convenience
	 */
	public TextCommandsBuilder disableHelpCommand(@NotNull Consumer<BaseCommandEvent> helpConsumer) {
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
	 * Adds a filter for received messages (could prevent regular commands from running), <b>See {@link BContext#addFilter(Predicate)} for more info</b>
	 *
	 * @param filter The filter to add, should return <code>false</code> if the message has to be ignored
	 * @return This builder for chaining convenience
	 * @see BContext#addFilter(Predicate)
	 */
	public TextCommandsBuilder addFilter(Predicate<MessageInfo> filter) {
		context.addFilter(filter);

		return this;
	}
}
