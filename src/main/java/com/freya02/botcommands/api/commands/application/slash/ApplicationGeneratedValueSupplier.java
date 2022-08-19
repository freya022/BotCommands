package com.freya02.botcommands.api.commands.application.slash;

import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ApplicationGeneratedValueSupplier {
	@Nullable
	Object getDefaultValue(@NotNull CommandInteractionPayload event);
}
