package com.freya02.bot.extensionsbot;

import com.freya02.botcommands.SettingsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySettingsProvider implements SettingsProvider {
	private final Map<Long, BGuildSettings> guildSettingsMap = new HashMap<>(); //Guild to settings map

	@Override
	public BGuildSettings getSettings(long guildId) {
		return guildSettingsMap.computeIfAbsent(guildId, id -> {
			final MyGuildSettings settings = new MyGuildSettings();

			if (id == 722891685755093072L) { //Only set ; as a prefix for this specific guild
				settings.prefixes.add(";");
			}

			return settings;
		});
	}

	public static class MyGuildSettings implements BGuildSettings {
		private final List<String> prefixes = new ArrayList<>();

		@Override
		public List<String> getPrefixes() {
			return prefixes;
		}
	}
}
