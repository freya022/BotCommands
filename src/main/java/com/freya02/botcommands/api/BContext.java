package com.freya02.botcommands.api;

import com.freya02.botcommands.api.commands.application.ApplicationCommandsContext;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer;
import com.freya02.botcommands.api.core.DefaultEmbedFooterIconSupplier;
import com.freya02.botcommands.api.core.DefaultEmbedSupplier;
import com.freya02.botcommands.api.core.GlobalExceptionHandler;
import com.freya02.botcommands.api.core.SettingsProvider;
import com.freya02.botcommands.api.core.config.*;
import com.freya02.botcommands.api.core.service.ServiceContainer;
import com.freya02.botcommands.api.core.service.annotations.InjectedService;
import kotlin.reflect.KClass;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@InjectedService
public interface BContext {
	@NotNull
	BConfig getConfig();

	@NotNull
	default BApplicationConfig getApplicationConfig() {
		return getConfig().getApplicationConfig();
	}

	@NotNull
	default BComponentsConfig getComponentsConfig() {
		return getConfig().getComponentsConfig();
	}

	@NotNull
	default BCoroutineScopesConfig getCoroutineScopesConfig() {
		return getConfig().getCoroutineScopesConfig();
	}

	@NotNull
	default BDebugConfig getDebugConfig() {
		return getConfig().getDebugConfig();
	}

	@NotNull
	default BServiceConfig getServiceConfig() {
		return getConfig().getServiceConfig();
	}

	@NotNull
	default BTextConfig getTextConfig() {
		return getConfig().getTextConfig();
	}

	//TODO docs
	@NotNull
    ServiceContainer getServiceContainer();

	//TODO docs
	@NotNull
	default <T> T getService(@NotNull KClass<T> clazz) {
		return getServiceContainer().getService(clazz);
	}

	//TODO docs
	@NotNull
	default <T> T getService(@NotNull Class<T> clazz) {
		return getServiceContainer().getService(clazz);
	}

	//TODO docs
	default <T> void putService(@NotNull T service) {
		getServiceContainer().putService(service);
	}

	/**
	 * Returns the JDA instance associated with this context
	 *
	 * @return the JDA instance of this context
	 */
	@NotNull
	JDA getJDA();

	@NotNull
	Status getStatus();

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
	 * Returns the {@link DefaultEmbedSupplier}
	 *
	 * @return The {@link DefaultEmbedSupplier}
	 * @see DefaultEmbedSupplier
	 */
	@NotNull
	DefaultEmbedSupplier getDefaultEmbedSupplier();

	/**
	 * Returns the {@link DefaultEmbedFooterIconSupplier}
	 *
	 * @return The {@link DefaultEmbedFooterIconSupplier}
	 * @see DefaultEmbedFooterIconSupplier
	 */
	@NotNull
	DefaultEmbedFooterIconSupplier getDefaultFooterIconSupplier();

	/**
	 * Sends an exception message to the unique bot owner, retrieved via {@link JDA#retrieveApplicationInfo()}
	 *
	 * @param message The message describing the context
	 * @param t       An optional exception
	 */
	void dispatchException(@NotNull String message, @Nullable Throwable t);

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
	 * Returns the {@link GlobalExceptionHandler global exception handler}, used to handle errors caught by the framework.
	 *
	 * @return The global exception handler
	 * @see GlobalExceptionHandler
	 */
	@Nullable
	GlobalExceptionHandler getGlobalExceptionHandler();

	/**
	 * Invalides the autocomplete cache of the specified autocomplete handler
	 * <br>This means that the cache of this autocomplete handler will be fully cleared
	 *
	 * @param autocompleteHandlerName The name of the autocomplete handler, supplied at {@link AutocompleteHandler#name()}
	 */
	void invalidateAutocompleteCache(String autocompleteHandlerName);

	enum Status {
		PRE_LOAD,
		LOAD,
		POST_LOAD,
		READY
	}
}
