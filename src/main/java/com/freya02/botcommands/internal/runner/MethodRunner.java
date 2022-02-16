package com.freya02.botcommands.internal.runner;

import com.freya02.botcommands.internal.ConsumerEx;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MethodRunner {
	/**
	 * <h2>WARN: This method may not block</h2>
	 */
	@NonBlocking
	default void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer) throws Exception {
		invoke(args, throwableConsumer, null);
	}

	/**
	 * <h2>WARN: This method may not block</h2>
	 */
	@NonBlocking
	<T> void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer, ConsumerEx<T> successCallback) throws Exception;
}
