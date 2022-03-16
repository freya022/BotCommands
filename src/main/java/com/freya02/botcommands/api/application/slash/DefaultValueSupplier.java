package com.freya02.botcommands.api.application.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@FunctionalInterface
public interface DefaultValueSupplier {
	Object getDefaultValue(SlashCommandInteractionEvent event);
}
