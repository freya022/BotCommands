package com.freya02.botcommands.api;

import com.freya02.botcommands.api.application.ApplicationCommandsContext;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.prefixed.HelpBuilderConsumer;
import kotlin.reflect.KClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
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
	 * @return Whether the bot will respond to its own ping
	 */
	boolean isPingAsPrefix();

	/**
	 * Returns the preferred prefix for triggering this bot
	 *
	 * @return The preferred prefix
	 */
	@NotNull
	default String getPrefix() {
		if (isPingAsPrefix()) {
			return getJDA().getSelfUser().getAsMention() + " ";
		}

		return getPrefixes().get(0);
	}

	/**
	 * Returns a list of IDs of the bot owners
	 *
	 * @return a list of IDs of the bot owners
	 */
	@NotNull
	Collection<Long> getOwnerIds();

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
	Supplier<@Nullable InputStream> getDefaultFooterIconSupplier();

	/**
	 * Sends an exception message to the unique bot owner, retrieved via {@link JDA#retrieveApplicationInfo()}
	 *
	 * @param message The message describing the context
	 * @param t       An optional exception
	 */
	void dispatchException(@NotNull String message, @Nullable Throwable t);

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
	HelpBuilderConsumer getHelpBuilderConsumer();

	/**
	 * Returns the uncaught exception handler
	 *
	 * @return The uncaught exception handler
	 * @see CommandsBuilder#setUncaughtExceptionHandler(ExceptionHandler)
	 */
	@Nullable
	ExceptionHandler getUncaughtExceptionHandler();

	/**
	 * Invalides the autocomplete cache of the specified autocomplete handler
	 * <br>This means that the cache of this autocomplete handler will be fully cleared
	 *
	 * @param autocompleteHandlerName The name of the autocomplete handler, supplied at {@link AutocompleteHandler#name()}
	 */
	void invalidateAutocompleteCache(String autocompleteHandlerName);
}
