package com.freya02.botcommands.api.builder;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.application.annotations.Test;
import com.freya02.botcommands.api.components.ComponentInteractionFilter;
import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationCommandsBuilder {
	private final BContextImpl context;

	private final List<Long> slashGuildIds = new ArrayList<>();

	public ApplicationCommandsBuilder(BContextImpl context) {
		this.context = context;
	}

	public List<Long> getSlashGuildIds() {
		return slashGuildIds;
	}

	/**
	 * Add an application command filter for this context
	 *
	 * @param commandFilter The application command filter to add
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see BContext#addApplicationFilter(ApplicationCommandFilter)
	 */
	@NotNull
	public ApplicationCommandsBuilder addApplicationFilter(@NotNull ApplicationCommandFilter commandFilter) {
		context.addApplicationFilter(commandFilter);

		return this;
	}

	/**
	 * Add a component interaction filter for this context
	 *
	 * @param componentFilter The component interaction filter to add
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see BContext#addComponentFilter(ComponentInteractionFilter)
	 */
	@NotNull
	public ApplicationCommandsBuilder addComponentFilter(@NotNull ComponentInteractionFilter componentFilter) {
		context.addComponentFilter(componentFilter);

		return this;
	}

	/**
	 * Debug feature - Makes it so application commands are only updated on these guilds
	 *
	 * @param slashGuildIds IDs of the guilds
	 *
	 * @return This builder for chaining convenience
	 */
	public ApplicationCommandsBuilder updateCommandsOnGuildIds(List<Long> slashGuildIds) {
		this.slashGuildIds.clear();
		this.slashGuildIds.addAll(slashGuildIds);

		return this;
	}

	/**
	 * Adds test guilds IDs for all commands annotated with {@link Test}
	 *
	 * @param guildIds The test {@link Guild} IDs
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see BContext#getTestGuildIds()
	 * @see Test
	 */
	public ApplicationCommandsBuilder addTestGuilds(long... guildIds) {
		context.addTestGuildIds(guildIds);

		return this;
	}

	/**
	 * Enables the library to do network calls to Discord in order to check if application commands need to be updated
	 * <br>It's better to leave it disk-based, it is faster and doesn't require any request to Discord
	 * <br><b>Online checks are to be avoided on production environments</b>, I strongly recommend you have a separate bot for tests purpose
	 * <p>
	 * <br>This option only makes sense if you work on your "development" bot is on multiple computers,
	 * as the files required for caching the already-pushed-commands are stored in your temporary files folder,
	 * another computer is not aware of it and might take <i>its own</i> files as being up-to-date, even if the commands on Discord are not.
	 * <br>This issue is fixed by using online checks
	 *
	 * @return This builder for chaining convenience
	 */
	public ApplicationCommandsBuilder enableOnlineAppCommandCheck() {
		context.enableOnlineAppCommandCheck();

		return this;
	}

	/**
	 * Sets whether all application commands should be guild-only, regardless of the command scope on the annotation
	 *
	 * @param force <code>true</code> to make all application commands as guild-only
	 *
	 * @return This builder for chaining convenience
	 */
	public ApplicationCommandsBuilder forceCommandsAsGuildOnly(boolean force) {
		context.getApplicationCommandsContext().setForceGuildCommands(force);

		return this;
	}

	/**
	 * Adds the specified bundle names with its locales, those bundles will be used for command localization (name, description, options, choices...)
	 * <br>All the locales will be considered as pointing to a valid localization bundle, logging a warning if it can't be found
	 * <br>See {@link DefaultLocalizationMapProvider} for default implementation details
	 *
	 * @param bundleName The name of the localization bundle
	 * @param locales    The locales the localization bundle supports
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see DefaultLocalizationMapProvider
	 */
	public ApplicationCommandsBuilder addLocalizations(@NotNull String bundleName, @NotNull DiscordLocale @NotNull ... locales) {
		return addLocalizations(bundleName, Arrays.asList(locales));
	}

	/**
	 * Adds the specified bundle names with its locales, those bundles will be used for command localization (name, description, options, choices...)
	 * <br>All the locales will be considered as pointing to a valid localization bundle, logging a warning if it can't be found
	 * <br>See {@link DefaultLocalizationMapProvider} for default implementation details
	 *
	 * @param bundleName The name of the localization bundle
	 * @param locales    The locales the localization bundle supports
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see DefaultLocalizationMapProvider
	 */
	public ApplicationCommandsBuilder addLocalizations(@NotNull String bundleName, @NotNull List<@NotNull DiscordLocale> locales) {
		context.getApplicationCommandsContext().addLocalizations(bundleName, locales);

		return this;
	}
}
