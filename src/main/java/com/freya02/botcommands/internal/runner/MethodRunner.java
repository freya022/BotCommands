package com.freya02.botcommands.internal.runner;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MethodRunner {
	void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer) throws Exception;
}
