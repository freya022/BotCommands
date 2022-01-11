package com.freya02.botcommands.api;

import com.freya02.botcommands.api.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandUpdateResult;
import com.freya02.botcommands.api.application.annotations.Test;
import com.freya02.botcommands.api.builder.ApplicationCommandsBuilder;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommandFilter;
import com.freya02.botcommands.internal.application.CommandInfoMap;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextCommandCandidates;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.runner.MethodRunnerFactory;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
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
	 * Return the {@link MethodRunnerFactory} for this context
	 *
	 * @return The {@link MethodRunnerFactory}
	 */
	MethodRunnerFactory getMethodRunnerFactory();

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
	DefaultMessages getDefaultMessages(@Nullable Guild guild);

	/**
	 * Returns the first occurrence of {@link TextCommandInfo} of the specified command name, the name can be an alias too
	 *
	 * @param path Name / alias of the command
	 * @return The {@link TextCommandInfo} object of the command name
	 */
	@Nullable
	TextCommandInfo findFirstCommand(@NotNull CommandPath path);

	/**
	 * Returns the text commands for the given path
	 *
	 * @param path The path of the command
	 * @return a {@link TextCommandCandidates list of text command info}
	 */
	@Nullable
	TextCommandCandidates findCommands(@NotNull CommandPath path);

	/**
	 * Returns the first occurrence of a text subcommand for the given path
	 *
	 * @param path The path of the command to find subcommands in
	 * @return a {@link TextCommandCandidates list of text command info}
	 */
	@Nullable
	TextCommandCandidates findFirstTextSubcommands(CommandPath path);

	/**
	 * Returns a list of text subcommands for the given path
	 *
	 * @param path The path of the command to find subcommands in
	 * @return a {@link List} of {@link TextCommandCandidates subcommand candidates}
	 */
	@Nullable
	List<TextCommandCandidates> findTextSubcommands(CommandPath path);

	/**
	 * Returns the {@link SlashCommandInfo} object of the specified full slash command name
	 *
	 * @param name Full name of the slash command (Examples: ban ; info/user ; ban/user/perm)
	 * @return The {@link SlashCommandInfo} object of the slash command
	 */
	@Nullable
	SlashCommandInfo findSlashCommand(@NotNull CommandPath name);

	/**
	 * Returns the {@link UserCommandInfo} object of the specified user context command name
	 *
	 * @param name Name of the user context command
	 * @return The {@link UserCommandInfo} object of the user context command
	 */
	@Nullable
	UserCommandInfo findUserCommand(@NotNull String name);

	/**
	 * Returns the {@link MessageCommandInfo} object of the specified message context command name
	 *
	 * @param name Name of the message context command
	 * @return The {@link MessageCommandInfo} object of the message context command
	 */
	@Nullable
	MessageCommandInfo findMessageCommand(@NotNull String name);

	/**
	 * Returns a view for all the registered application commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the application commands
	 */
	@NotNull
	@UnmodifiableView
	ApplicationCommandInfoMapView getApplicationCommandInfoMapView();

	/**
	 * Returns a view for all the registered slash commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the slash commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<SlashCommandInfo> getSlashCommandsMapView();

	/**
	 * Returns a view for all the registered user context commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the user context commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<UserCommandInfo> getUserCommandsMapView();

	/**
	 * Returns a view for all the registered message context commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the message context commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<MessageCommandInfo> getMessageCommandsMapView();

	/**
	 * Returns a list of the application commands paths, names such as <code>ban/user/perm</code>
	 *
	 * @return A list of the application commands paths
	 */
	List<CommandPath> getSlashCommandsPaths();

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
	 * Adds a text command filter for the command listener to check on each <b>regular / regex</b> command
	 * <br>If one of the filters returns <code>false</code>, then the command is not executed
	 * <br>Command overloads are also not executed
	 *
	 * <h2>Example</h2>
	 * <h3>Restricting the bot to a certain {@link GuildMessageChannel}</h3>
	 * <pre><code>
	 * CommandsBuilder.newBuilder()
	 *      .textCommandBuilder(textCommandsBuilder -> textCommandsBuilder
	 *          .addTextFilter(data -> data.event().getChannel().getIdLong() == 722891685755093076L)
	 *      )
	 * </code></pre>
	 *
	 * @param filter The filter to add
	 */
	void addTextFilter(TextCommandFilter filter);

	/**
	 * Adds a filter for the application command listener, this will check slash commands as well as context commands
	 * <br>If one of the filters returns <code>false</code>, then the command is not executed
	 * <br><b>You still have to reply to the interaction !</b>
	 *
	 * @param filter The filter to add
	 */
	void addApplicationFilter(ApplicationCommandFilter filter);

	/**
	 * Removes a previously set text command filter
	 *
	 * @param filter The filter to remove
	 * @see #addTextFilter(TextCommandFilter)
	 */
	void removeTextFilter(TextCommandFilter filter);

	/**
	 * Removes a previously set application command filter
	 *
	 * @param filter The filter to remove
	 * @see #addApplicationFilter(ApplicationCommandFilter)
	 */
	void removeApplicationFilter(ApplicationCommandFilter filter);

	/**
	 * Overrides the default help given for text commands
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
	void addRegistrationListeners(RegistrationListener... listeners);

	/**
	 * Returns the component manager of this instance
	 *
	 * @return The component manager
	 */
	@Nullable
	ComponentManager getComponentManager();

	/**
	 * Returns the {@linkplain SettingsProvider} for this context
	 *
	 * @return The current {@linkplain SettingsProvider}
	 */
	@Nullable
	SettingsProvider getSettingsProvider();

	/**
	 * Returns the {@link Locale} for the specified {@link Guild}
	 *
	 * @param guild The {@link Guild} in which to take the {@link Locale} from
	 * @return The {@link Locale} of the {@link Guild}
	 */
	@NotNull
	default Locale getEffectiveLocale(@Nullable Guild guild) {
		final SettingsProvider provider = getSettingsProvider();
		if (provider == null) return Locale.getDefault();

		return provider.getLocale(guild);
	}

	/**
	 * Returns the help builder consumer - changes the EmbedBuilder given to add more stuff in it
	 *
	 * @return The help builder consumer
	 */
	Consumer<EmbedBuilder> getHelpBuilderConsumer();

	/**
	 * Updates the application commands and their permissions in the specified guilds <br><br>
	 * Why you could call this method:
	 * <ul>
	 *     <li>Your bot joins a server and you wish to add a guild command to it </li>
	 *     <li>An admin changes the permissions of a guild application-command in your bot</li>
	 *     <li>You decide to remove a command from a guild while the bot is running, <b>I do not mean code hotswap! It will not work that way</b></li>
	 * </ul>
	 *
	 * @param guilds Iterable collection of the guilds to update
	 * @param force  Whether the commands and permissions should be updated no matter what
	 * @param onlineCheck Whether the commands should be updated by checking Discord, see {@link ApplicationCommandsBuilder#enableOnlineAppCommandCheck()}
	 * @return A {@link Map} of {@link Guild} to their {@link CommandUpdateResult} {@link CompletableFuture completable futures}
	 */
	@NotNull
	Map<Guild, CompletableFuture<CommandUpdateResult>> scheduleApplicationCommandsUpdate(Iterable<Guild> guilds, boolean force, boolean onlineCheck);

	/**
	 * Updates the application commands and their permissions in the specified guild <br><br>
	 * Why you could call this method:
	 * <ul>
	 *     <li>Your bot joins a server and you wish to add a guild command to it </li>
	 *     <li>An admin changes the permissions of a guild application-command in your bot</li>
	 *     <li>You decide to remove a command from a guild while the bot is running, <b>I do not mean code hotswap! It will not work that way</b></li>
	 * </ul>
	 *
	 * @param guild The guild which needs to be updated
	 * @param force Whether the commands and permissions should be updated no matter what
	 * @param onlineCheck Whether the commands should be updated by checking Discord, see {@link ApplicationCommandsBuilder#enableOnlineAppCommandCheck()}
	 * @return A {@link CommandUpdateResult} {@link CompletableFuture completable future}
	 */
	@NotNull
	CompletableFuture<CommandUpdateResult> scheduleApplicationCommandsUpdate(Guild guild, boolean force, boolean onlineCheck);

	/**
	 * Register a custom resolver for interaction commands (components / app commands)
	 *
	 * @param parameterType Type of the parameter
	 * @param function      Supplier function, may receive interaction events of any type
	 * @param <T>           Type of the parameter
	 */
	<T> void registerCustomResolver(Class<T> parameterType, Function<Event, T> function);

	/**
	 * Returns the uncaught exception handler
	 *
	 * @return The uncaught exception handler
	 * @see CommandsBuilder#setUncaughtExceptionHandler(ExceptionHandler)
	 */
	@Nullable
	ExceptionHandler getUncaughtExceptionHandler();

	/**
	 * Returns the test guilds IDs, slash commands annotated with {@link Test @Test} will only be included in these guilds
	 *
	 * @return The set of test guild IDs
	 */
	TLongSet getTestGuildIds();
}
