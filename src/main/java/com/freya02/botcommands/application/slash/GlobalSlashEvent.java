package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.SlashCommandInteractionImpl;

import javax.annotation.Nonnull;

public abstract class GlobalSlashEvent extends SlashCommandEvent {
	public GlobalSlashEvent(@Nonnull JDA api, long responseNumber, @Nonnull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);
	}

	public abstract BContext getContext();
}
