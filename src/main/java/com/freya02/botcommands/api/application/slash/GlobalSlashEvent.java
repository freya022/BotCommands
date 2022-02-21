package com.freya02.botcommands.api.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.localization.Localization;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class GlobalSlashEvent extends SlashCommandInteractionEvent {
	public GlobalSlashEvent(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);
	}

	public abstract BContext getContext();

	@NotNull
	public abstract String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	@NotNull
	public String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), localizationPath, entries);
	}

	@NotNull
	public String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), localizationPath, entries);
	}
}
