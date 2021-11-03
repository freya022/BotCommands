package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.PaginationSupplier;

record InteractiveMenuItem(SelectContent content, PaginationSupplier supplier) {}