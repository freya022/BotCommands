package com.freya02.botcommands.test;

import com.freya02.botcommands.api.core.SettingsProvider;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@ServiceType(types = SettingsProvider.class)
public class BasicSettingsProvider implements SettingsProvider {
	@Override
	@NotNull
	public DiscordLocale getLocale(@Nullable Guild guild) {
		if (guild != null) {
			if (guild.getIdLong() == 722891685755093072L) {
				return DiscordLocale.ENGLISH_UK; //not default on my system
			}
		}

		return SettingsProvider.super.getLocale(guild);
	}

	@Override
	public boolean doesUserConsentNSFW(@NotNull User user) {
		return true;
	}
}