package com.freya02.botcommands;

import com.freya02.botcommands.application.GuildApplicationSettings;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public interface SettingsProvider extends GuildApplicationSettings {
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
	 * <br><i>You can have a list of command names if needed in {@link BContext#getSlashCommandsPaths()} ()}</i>
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

	@NotNull
	default Locale getLocale(@Nullable Guild guild) {
		return Locale.getDefault();
	}
}
