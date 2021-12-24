package com.freya02.botcommands.internal.runner;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public abstract class MethodRunnerFactory {
	@NotNull
	public abstract MethodRunner make(Object instance, Method method);

	public abstract boolean supportsSuspend();

	public abstract boolean isSuspend(Method method);
}
