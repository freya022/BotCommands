package com.freya02.botcommands.internal.runner;

import com.freya02.botcommands.internal.ConsumerEx;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class JavaMethodRunner implements MethodRunner {
	private final Object instance;
	private final Method method;

	public JavaMethodRunner(Object instance, Method method) {
		this.instance = instance;
		this.method = method;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer, ConsumerEx<T> successCallback) throws Exception {
		//Try catching and threading are not needed, this code runs on its own thread already and is wrapped by a throwable try catch
		final Object returnValue = method.invoke(instance, args);
		if (successCallback != null) {
			successCallback.accept((T) returnValue);
		}
	}
}
