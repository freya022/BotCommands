package com.freya02.botcommands.api.pagination.interactive;

record InteractiveMenuItem<R extends BasicInteractiveMenu<R>>(SelectContent content, int maxPages, InteractiveMenuSupplier<R> supplier) {}