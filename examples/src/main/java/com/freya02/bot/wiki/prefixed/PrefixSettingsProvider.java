package com.freya02.bot.wiki.prefixed;

import com.freya02.botcommands.api.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrefixSettingsProvider implements SettingsProvider {
	@Override
	@Nullable
	public List<String> getPrefixes(@NotNull Guild guild) {
		if (guild.getIdLong() == 722891685755093072L) {
			return List.of("^"); //Only the prefix "^" will be used for the guild ID above
		}

		return SettingsProvider.super.getPrefixes(guild);
	}
}