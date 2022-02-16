package com.freya02.botcommands.api.pagination.interactive;

record InteractiveMenuItem<R extends BasicInteractiveMenu<R>>(SelectContent content, InteractiveMenuSupplier<R> supplier) {}