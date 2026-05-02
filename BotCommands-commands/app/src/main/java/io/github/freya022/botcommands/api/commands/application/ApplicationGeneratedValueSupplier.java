package io.github.freya022.botcommands.api.commands.application;

import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface ApplicationGeneratedValueSupplier {
    @Nullable
    Object getDefaultValue(CommandInteractionPayload event);
}
