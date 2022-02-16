package com.freya02.botcommands.test;

import com.freya02.botcommands.api.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BasicSettingsProvider implements SettingsProvider {
	@Override
	@NotNull
	public Locale getLocale(@Nullable Guild guild) {
		if (guild != null) {
			if (guild.getIdLong() == 722891685755093072L) {
				return Locale.ENGLISH; //not default on my system
			}
		}

		return SettingsProvider.super.getLocale(guild);
	}

	@Override
	public boolean doesUserConsentNSFW(@NotNull User user) {
		return true;
	}
}