package com.freya02.botcommands.internal.utils;

import kotlinx.coroutines.TimeoutCancellationException;
import org.jetbrains.annotations.NotNull;

public class TimeoutExceptionAccessor {
    @NotNull
    public static TimeoutCancellationException createComponentTimeoutException() {
        return new TimeoutCancellationException("Timed out waiting for component");
    }

    @NotNull
    public static TimeoutCancellationException createModalTimeoutException() {
        return new TimeoutCancellationException("Timed out waiting for modal");
    }
}
