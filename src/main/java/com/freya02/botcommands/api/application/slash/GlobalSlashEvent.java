package com.freya02.botcommands.api.application.slash;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public abstract class GlobalSlashEvent extends SlashCommandInteractionEvent {
	public GlobalSlashEvent(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);
	}

	public abstract BContext getContext();
}
