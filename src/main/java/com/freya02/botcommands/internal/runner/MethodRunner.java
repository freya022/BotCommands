package com.freya02.botcommands.internal.runner;

import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MethodRunner {
	/**
	 * <h2>WARN: This method may not block</h2>
	 */
	@NonBlocking
	void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer) throws Exception;
}
