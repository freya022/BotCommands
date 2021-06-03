package com.freya02.botcommands;

import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.MessageInfo;
import com.freya02.botcommands.slash.SlashCommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface BContext {
	/**
	 * Returns the JDA instance associated with this context
	 *
	 * @return the JDA instance of this context
	 */
	@NotNull
	JDA getJDA();

	/**
	 * Returns the full list of prefixes used to trigger the bot
	 *
	 * @return Full list of prefixes
	 */
	@NotNull
	List<String> getPrefixes();

	/**
	 * Returns the preferred prefix for triggering this bot
	 *
	 * @return The preferred prefix
	 */
	@NotNull
	default String getPrefix() {
		return getPrefixes().get(0);
	}

	/**
	 * Adds a prefix to choose from
	 *
	 * @param prefix The prefix to add
	 */
	void addPrefix(String prefix);

	/**
	 * Returns a list of IDs of the bot owners
	 *
	 * @return a list of IDs of the bot owners
	 */
	@NotNull
	List<Long> getOwnerIds();

	/**
	 * Tells whether this user is an owner or not
	 *
	 * @param userId ID of the user
	 * @return <code>true</code> if the user is an owner
	 */
	default boolean isOwner(long userId) {
		return getOwnerIds().contains(userId);
	}

	@NotNull
	DefaultMessages getDefaultMessages();

	/**
	 * Returns the {@linkplain Command} object of the specified command name, the name can be an alias too
	 *
	 * @param name Name / alias of the command
	 * @return The {@linkplain Command} object of the command name
	 */
	@Nullable
	Command findCommand(@NotNull String name);

	@Nullable
	SlashCommandInfo findSlashCommand(@NotNull String name);

	/**
	 * Returns the default {@linkplain EmbedBuilder} supplier
	 *
	 * @return The default {@linkplain EmbedBuilder} supplier
	 * @see CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)
	 */
	@NotNull
	Supplier<EmbedBuilder> getDefaultEmbedSupplier();

	/**
	 * Returns the default icon {@linkplain InputStream} supplier
	 *
	 * @return The default icon {@linkplain InputStream} supplier
	 * @see CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)
	 */
	@NotNull
	Supplier<InputStream> getDefaultFooterIconSupplier();

	/**
	 * Adds a filter for the command listener to check on each command
	 * @param filter The filter to add
	 */
	void addFilter(Predicate<MessageInfo> filter);

	/**
	 * Removes a previously set filter
	 * @param filter The filter to remove
	 */
	void removeFilter(Predicate<MessageInfo> filter);

	/**
	 * Overrides the default help given in {@linkplain Command#showHelp(BaseCommandEvent)}
	 * @param helpConsumer Help function to use when a command is recognized but syntax is invalid
	 */
	void overrideHelp(Consumer<BaseCommandEvent> helpConsumer);

	/**
	 * Returns the help consumer used when commands are found but not understood
	 *
	 * @return Consumer which should output help
	 */
	Consumer<BaseCommandEvent> getHelpConsumer();
}
