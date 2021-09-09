package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.internal.interactions.commands.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public abstract class GlobalSlashEvent extends SlashCommandEvent {
	public GlobalSlashEvent(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);
	}

	public abstract BContext getContext();
}
