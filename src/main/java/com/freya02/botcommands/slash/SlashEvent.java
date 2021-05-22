package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.interactions.CommandInteractionImpl;

import javax.annotation.Nonnull;

public abstract class SlashEvent extends SlashCommandEvent {
	public SlashEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);
	}

	public abstract BContext getContext();
}
