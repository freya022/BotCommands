package io.github.freya022.botcommands.api.pagination.interactive;

public record InteractiveMenuItem<R extends BasicInteractiveMenu<R>>(SelectContent content, int maxPages, InteractiveMenuSupplier<R> supplier) {}