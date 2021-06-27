package com.freya02.botcommands;

import com.freya02.botcommands.buttons.IdManager;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.MessageInfo;
import com.freya02.botcommands.slash.SlashCommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
	 * Adds a filter for the command listener to check on each <b>regular / regex</b> command<br>
	 * If one of the filters returns false, then the command is skipped, not executed
	 *
	 * <h2>Example</h2>
	 * <h3>Restricting the bot to a certain TextChannel</h3>
	 * <pre><code>
final CommandsBuilder builder = CommandsBuilder.withPrefix(":", 222046562543468545L);
builder.getContext().addFilter(messageInfo{@literal ->} messageInfo.getEvent().getChannel().getIdLong() == 722891685755093076L);
	 * </code></pre>
	 *
	 * @param filter The filter to add
	 */
	void addFilter(Predicate<MessageInfo> filter);

	/**
	 * Removes a previously set filter
	 *
	 * @param filter The filter to remove
	 * @see #addFilter(Predicate)
	 */
	void removeFilter(Predicate<MessageInfo> filter);

	/**
	 * Overrides the default help given in {@linkplain Command#showHelp(BaseCommandEvent)}
	 *
	 * @param helpConsumer Help function to use when a command is recognized but syntax is invalid
	 */
	void overrideHelp(Consumer<BaseCommandEvent> helpConsumer);

	/**
	 * Returns the help consumer used when commands are found but not understood
	 *
	 * @return Consumer which should output help
	 */
	Consumer<BaseCommandEvent> getHelpConsumer();

	/**
	 * Returns an immutable list of the registration listeners
	 *
	 * @return Immutable list of the registration listeners
	 */
	List<RegistrationListener> getRegistrationListeners();

	/**
	 * Adds registration listeners
	 *
	 * @param listeners Registration listeners to add
	 */
	void addRegistrationListener(RegistrationListener... listeners);

	/**
	 * Returns the ID manager of this instance
	 *
	 * @return The ID manager
	 */
	@Nullable IdManager getIdManager();

	/**
	 * Returns the {@linkplain PermissionProvider} for this context
	 *
	 * @return The {@linkplain PermissionProvider} for this context
	 */
	PermissionProvider getPermissionProvider();

	/**
	 * Returns the {@linkplain SettingsProvider} for this context
	 *
	 * @return The current {@linkplain SettingsProvider}
	 */
	@Nullable
	SettingsProvider getSettingsProvider();

	/**
	 * Returns the {@linkplain BGuildSettings Guild settings} for the given Guild ID
	 *
	 * @param guildId The Guild ID to get the settings from
	 * @return This guild-specific settings
	 */
	@Nullable
	default BGuildSettings getGuildSettings(long guildId) {
		final SettingsProvider settingsProvider = getSettingsProvider();
		if (settingsProvider == null) return null;

		return settingsProvider.getSettings(guildId);
	}

	/**
	 * Returns the help builder consumer - changes the EmbedBuilder given to add more stuff in it
	 *
	 * @return The help builder consumer
	 */
	Consumer<EmbedBuilder> getHelpBuilderConsumer();

	/**
	 * Updates the slash commands and their permissions in the specified guilds <br><br>
	 * Examples:
	 * <ul>
	 *     <li>Your bot joins a server and you wish to add a guild command to it
	 *          <ul><li>You need to update the commands with this</li></ul>
	 *     </li>
	 *     <li>An admin changes the permissions of a guild slash-command in your bot
	 *          <ul><li>You need to update the permissions with this</li></ul>
	 *     </li>
	 * </ul>
	 *
	 * @param guilds Iterable collection of the guilds to update
	 * @return <code>true</code> if one or more command / permission were changed, <code>false</code> if none changed
	 * @throws IOException If unable to write the cache data
	 */
	boolean tryUpdateGuildCommands(Iterable<Guild> guilds) throws IOException;
}
