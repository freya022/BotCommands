package com.freya02.botcommands.internal.runner;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class JavaMethodRunnerFactory extends MethodRunnerFactory {
	@Override
	@NotNull
	public MethodRunner make(Object instance, Method method) {
		return new JavaMethodRunner(instance, method);
	}

	@Override
	public boolean supportsSuspend() {
		return false;
	}

	@Override
	public boolean isSuspend(Method method) {
		return false;
	}
}
