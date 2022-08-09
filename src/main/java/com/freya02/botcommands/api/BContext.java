package com.freya02.botcommands.api;

import com.freya02.botcommands.annotations.api.application.annotations.Test;
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.application.ApplicationCommandsContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.parameters.CustomResolverFunction;
import com.freya02.botcommands.api.prefixed.HelpConsumer;
import com.freya02.botcommands.api.prefixed.TextCommandFilter;
import com.freya02.botcommands.internal.prefixed.TextCommandCandidates;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import gnu.trove.set.TLongSet;
import kotlin.reflect.KClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface BContext {
	//TODO docs
	<T> T getService(KClass<T> clazz);

	//TODO docs
	<T> T getService(Class<T> clazz);

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
	DefaultMessages getDefaultMessages(@NotNull DiscordLocale locale);

	/**
	 * Returns the {@link DefaultMessages} instance for this Guild's locale
	 *
	 * @param guild The Guild to take the locale from
	 *
	 * @return The {@link DefaultMessages} instance with the Guild's locale
	 */
	@NotNull
	default DefaultMessages getDefaultMessages(@Nullable Guild guild) {
		return getDefaultMessages(getEffectiveLocale(guild));
	}

	/**
	 * Returns the {@link DefaultMessages} instance for this user's locale
	 *
	 * @param interaction The Interaction to take the user's locale from
	 *
	 * @return The {@link DefaultMessages} instance with the user's locale
	 */
	@NotNull
	default DefaultMessages getDefaultMessages(@NotNull Interaction interaction) {
		return getDefaultMessages(interaction.getUserLocale());
	}

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
	 * Returns the application commands context, this is for user/message/slash commands and related methods
	 *
	 * @return The {@link ApplicationCommandsContext} object
	 */
	@NotNull
	ApplicationCommandsContext getApplicationCommandsContext();

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
	 * Sends an exception message to the unique bot owner, retrieved via {@link JDA#retrieveApplicationInfo()}
	 *
	 * @param message The message describing the context
	 * @param t       An optional exception
	 */
	void dispatchException(@NotNull String message, @Nullable Throwable t);

	/**
	 * Adds a text command filter for the command listener to check on each <b>regular / regex</b> command
	 * <br>If one of the filters returns <code>false</code>, then the command is not executed
	 * <br>Command overloads are also not executed
	 *
	 * <p>
	 * <br><b>Example</b>
	 * <br><b>Restricting the bot to a certain {@link GuildMessageChannel}</b>
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
	void overrideHelp(HelpConsumer helpConsumer);

	/**
	 * Returns the help consumer used when commands are found but not understood
	 *
	 * @return Consumer which should output help
	 */
	HelpConsumer getHelpConsumer();

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
	 * Returns the {@link DiscordLocale} for the specified {@link Guild}
	 *
	 * @param guild The {@link Guild} in which to take the {@link DiscordLocale} from
	 *
	 * @return The {@link DiscordLocale} of the {@link Guild}
	 */
	@NotNull
	default DiscordLocale getEffectiveLocale(@Nullable Guild guild) {
		if (guild != null && guild.getFeatures().contains("COMMUNITY")) {
			return guild.getLocale();
		}

		final SettingsProvider provider = getSettingsProvider();
		if (provider == null) return DiscordLocale.ENGLISH_US; //Discord default

		return provider.getLocale(guild);
	}

	/**
	 * Returns the help builder consumer - changes the EmbedBuilder given to add more stuff in it
	 *
	 * @return The help builder consumer
	 */
	Consumer<EmbedBuilder> getHelpBuilderConsumer();

	/**
	 * Register a custom resolver for interaction commands (components / app commands)
	 *
	 * @param parameterType Type of the parameter
	 * @param function      Supplier function, may receive interaction events of any type
	 * @param <T>           Type of the parameter
	 */
	<T> void registerCustomResolver(Class<T> parameterType, CustomResolverFunction<T> function);

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

	/**
	 * Invalides the autocompletion cache of the specified autocompletion handler
	 * <br>This means that the cache of this autocompletion handler will be fully cleared
	 *
	 * @param autocompletionHandlerName The name of the autocompletion handler, supplied at {@link AutocompletionHandler#name()}
	 */
	void invalidateAutocompletionCache(String autocompletionHandlerName);
}
