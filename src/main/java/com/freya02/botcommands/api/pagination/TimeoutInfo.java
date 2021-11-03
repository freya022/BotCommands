package com.freya02.botcommands.api.pagination;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public record TimeoutInfo<T extends BasicPagination<T>>(long timeout, @NotNull TimeUnit unit, @NotNull PaginationTimeoutConsumer<T> onTimeout) {}