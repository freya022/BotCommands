package com.freya02.bot.extensionsbot;

import com.freya02.botcommands.api.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MySettingsProvider implements SettingsProvider {
	@Override
	public @Nullable List<String> getPrefixes(@NotNull Guild guild) {
		if (guild.getIdLong() == 722891685755093072L) { //Only set ; as a prefix for this specific guild
			return List.of(";");
		}

		return SettingsProvider.super.getPrefixes(guild);
	}
}
