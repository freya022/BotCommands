package io.github.freya022.botcommands.internal.utils;

import kotlinx.coroutines.TimeoutCancellationException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TimeoutExceptionAccessor {
    public static TimeoutCancellationException createComponentTimeoutException() {
        return new TimeoutCancellationException("Timed out waiting for component");
    }

    public static TimeoutCancellationException createModalTimeoutException() {
        return new TimeoutCancellationException("Timed out waiting for modal");
    }
}
