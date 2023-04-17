package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.DefaultMessages;
import com.freya02.botcommands.api.commands.CommandList;
import com.freya02.botcommands.api.commands.application.ApplicationCommandsContext;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.InterfacedService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Interface for settings requested by the framework, such as prefixes, guild locale or guild commands whitelist.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService}, and a {@link ServiceType} of {@link SettingsProvider}.
 *
 * @see InterfacedService
 */
@InterfacedService
public interface SettingsProvider {
	/**
	 * Returns the list of prefix this Guild should use <br>
	 * <b>If the returned list is null or empty, the global prefixes will be used</b>
	 *
	 * @return The list of prefixes
	 */
	@Nullable
	default List<String> getPrefixes(@NotNull Guild guild) {
		return null;
	}

	/**
	 * Returns the list of guild commands usable in that Guild
	 * <br><i>You can have a list of command names if needed in {@link ApplicationCommandsContext#getSlashCommandsPaths()} ()}</i>
	 *
	 * @return A CommandList of this guild's commands
	 * @see CommandList#all()
	 * @see CommandList#none()
	 * @see CommandList#of(Collection)
	 * @see CommandList#notOf(Collection)
	 * @see CommandList#filter(Predicate)
	 */
	@NotNull
	default CommandList getGuildCommands(@NotNull Guild guild) {
		return CommandList.all();
	}

	/**
	 * Returns the {@link Locale} of the given {@link Guild}, will be null for a global context
	 * <br>This might be used for localization such as in default messages or application commands
	 *
	 * @param guild The target {@link Guild} to get the {@link Locale} from
	 * @return The {@link Locale} of the specified guild
	 * @see DefaultMessages
	 */
	@NotNull
	default DiscordLocale getLocale(@Nullable Guild guild) {
		if (guild != null) return guild.getLocale();

		//Discord default locale is US english
		return DiscordLocale.ENGLISH_US;
	}

	/**
	 * Returns whether the specified {@link User} consents to executing NSFW commands in its DMs
	 * <br>Note: <b>You</b> may also use this method to know if a user consents to getting NSFW content from other users
	 *
	 * @param user The {@link User} which would receive NSFW content
	 * @return <code>true</code> if the {@link User} is consenting to NSFW content, <code>false</code> otherwise
	 */
	default boolean doesUserConsentNSFW(@NotNull User user) {
		return false;
	}
}
