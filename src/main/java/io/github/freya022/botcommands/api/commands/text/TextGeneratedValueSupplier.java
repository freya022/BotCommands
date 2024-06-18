package io.github.freya022.botcommands.api.commands.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TextGeneratedValueSupplier {
    @Nullable
    Object getDefaultValue(@NotNull BaseCommandEvent event);
}
