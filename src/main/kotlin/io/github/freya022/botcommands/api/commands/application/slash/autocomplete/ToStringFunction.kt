package io.github.freya022.botcommands.api.commands.application.slash.autocomplete;

import org.jetbrains.annotations.NotNull;

public interface ToStringFunction<T> {
    @NotNull
    String toString(T item);
}
