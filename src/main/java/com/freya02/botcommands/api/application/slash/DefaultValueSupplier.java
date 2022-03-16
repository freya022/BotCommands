package com.freya02.botcommands.api.application.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DefaultValueSupplier {
	@Nullable
	Object getDefaultValue(@NotNull SlashCommandInteractionEvent event);
}
