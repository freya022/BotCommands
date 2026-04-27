package io.github.freya022.botcommands.api.commands.text;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface TextGeneratedValueSupplier {
    @Nullable
    Object getDefaultValue(BaseCommandEvent event);
}
