package com.freya02.botcommands.internal;

import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunner;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public interface ExecutableInteractionInfo {
	@NotNull
	Method getMethod();

	@NotNull
	MethodRunner getMethodRunner();

	@NotNull
	MethodParameters<? extends CommandParameter<?>> getParameters();

	@NotNull
	default List<? extends CommandParameter<?>> getOptionParameters() {
		return getParameters().stream().filter(CommandParameter::isOption).collect(Collectors.toList());
	}

	@NotNull
	Object getInstance();
}
